package tn.fst.backend.backend.service;

import tn.fst.backend.backend.entity.Payment;
import tn.fst.backend.backend.entity.Order;
import tn.fst.backend.backend.repository.PaymentRepository;
import tn.fst.backend.backend.repository.OrderRepository;
import tn.fst.backend.backend.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.StripePaymentRequest;
import tn.fst.backend.backend.dto.StripePaymentResponse;
import tn.fst.backend.backend.entity.*;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;

import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    @Override
    public Payment createPayment(Payment payment) {
        if (payment.getOrder() != null) {
            orderRepository.findById(payment.getOrder().getIdOrder())
                    .ifPresent(payment::setOrder);
        }
        Payment saved = paymentRepository.save(payment);
        syncInvoiceStatusFromPayment(saved);
        return saved;
    }

    @Override
    public Payment updatePayment(Long id, Payment paymentDetails) {
        Optional<Payment> optional = paymentRepository.findById(id);
        if (!optional.isPresent())
            throw new RuntimeException("Payment not found with id: " + id);

        Payment payment = optional.get();
        payment.setPaymentMethod(paymentDetails.getPaymentMethod());
        payment.setStatus(paymentDetails.getStatus());
        payment.setDatePayment(paymentDetails.getDatePayment());

        if (paymentDetails.getOrder() != null)
            orderRepository.findById(paymentDetails.getOrder().getIdOrder())
                    .ifPresent(payment::setOrder);

        Payment saved = paymentRepository.save(payment);
        syncInvoiceStatusFromPayment(saved);
        return saved;
    }

    @Override
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id))
            throw new RuntimeException("Payment not found with id: " + id);
        paymentRepository.deleteById(id);
    }

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final AccountingAutomationService accountingAutomationService;

    @Value("${stripe.api.key:sk_test_default}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    /**
     * Create Stripe Payment Intent
     * This generates a client secret that frontend uses to collect payment
     */
    public StripePaymentResponse createPaymentIntent(StripePaymentRequest request) throws StripeException {

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(request.getAmount()) // Amount in cents
                .setCurrency(request.getCurrency().toLowerCase())
                .setDescription(request.getDescription() != null ? request.getDescription() : "Order payment")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        return StripePaymentResponse.builder()
                .clientSecret(intent.getClientSecret())
                .paymentIntentId(intent.getId())
                .build();
    }

    /**
     * Confirm payment and create payment record
     */
    public Payment confirmPayment(Long orderId, String paymentIntentId, String paymentMethod) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Verify payment with Stripe
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            if (!"succeeded".equals(intent.getStatus())) {
                throw new IllegalStateException("Payment not successful. Status: " + intent.getStatus());
            }

            // Create payment record
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(PaymentMethod.valueOf(paymentMethod.toUpperCase()))
                    .status(PaymentStatus.COMPLETED)
                    .amount(order.getTotalAmount())
                    .currency("USD") // Or get from order
                    .transactionId(intent.getId())
                    .paymentIntentId(paymentIntentId)
                    .paidAt(java.time.LocalDateTime.now())
                    .build();

            Payment saved = paymentRepository.save(payment);
            syncInvoiceStatusFromPayment(saved);
            return saved;

        } catch (StripeException e) {
            // Payment verification failed
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(PaymentMethod.valueOf(paymentMethod.toUpperCase()))
                    .status(PaymentStatus.FAILED)
                    .amount(order.getTotalAmount())
                    .currency("USD")
                    .paymentIntentId(paymentIntentId)
                    .failureReason(e.getMessage())
                    .build();

            Payment saved = paymentRepository.save(payment);
            syncInvoiceStatusFromPayment(saved);
            return saved;
        }
    }

    /**
     * Record Cash on Delivery payment
     */
    public Payment createCODPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .status(PaymentStatus.PENDING)
                .amount(order.getTotalAmount())
                .currency("USD")
                .build();

        Payment saved = paymentRepository.save(payment);
        syncInvoiceStatusFromPayment(saved);
        return saved;
    }

    /**
     * Get payment by order
     */
    @Transactional(readOnly = true)
    public Payment getPaymentByOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        return paymentRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));
    }

    /**
     * Refund payment (for cancellations/returns)
     */
    public Payment refundPayment(Long paymentId) throws StripeException {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if (PaymentMethod.STRIPE.equals(payment.getPaymentMethod()) && payment.getPaymentIntentId() != null) {
            // Create refund in Stripe
            com.stripe.model.Refund refund = com.stripe.model.Refund.create(
                    com.stripe.param.RefundCreateParams.builder()
                            .setPaymentIntent(payment.getPaymentIntentId())
                            .build());

            if ("succeeded".equals(refund.getStatus())) {
                payment.setStatus(PaymentStatus.REFUNDED);
                Payment saved = paymentRepository.save(payment);
                syncInvoiceStatusFromPayment(saved);
                return saved;
            }
        }

        throw new IllegalStateException("Cannot refund this payment");
    }

    private void syncInvoiceStatusFromPayment(Payment payment) {
        if (payment == null || payment.getOrder() == null) {
            return;
        }

        Order order = payment.getOrder();

        Invoice invoice = invoiceRepository.findByOrder(order).orElseGet(() -> {
            try {
                invoiceService.issueDate(order.getIdOrder());
            } catch (Exception ignored) {
                // Keep payment flow resilient; invoice automation can be retried.
            }
            return invoiceRepository.findByOrder(order).orElse(null);
        });

        if (invoice == null) {
            return;
        }

        InvoiceStatus previousStatus = invoice.getStatus();

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            invoice.setStatus(InvoiceStatus.PAID);
            if (invoice.getPaidDate() == null) {
                invoice.setPaidDate(java.time.LocalDate.now());
            }
        } else if (payment.getStatus() == PaymentStatus.REFUNDED) {
            invoice.setStatus(InvoiceStatus.REFUNDED);
        } else if (payment.getStatus() == PaymentStatus.FAILED) {
            invoice.setStatus(InvoiceStatus.ISSUED);
        }

        invoice = invoiceRepository.save(invoice);

        if (previousStatus != invoice.getStatus()) {
            if (invoice.getStatus() == InvoiceStatus.PAID) {
                accountingAutomationService.recordInvoicePaid(invoice);
            } else if (invoice.getStatus() == InvoiceStatus.REFUNDED) {
                accountingAutomationService.recordInvoiceRefunded(invoice);
            }
        }
    }
}
