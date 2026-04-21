package tn.fst.backend.backend.service;

import tn.fst.backend.backend.dto.ReturnRequest;
import tn.fst.backend.backend.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    List<Payment> getAllPayments();

    Optional<Payment> getPaymentById(Long id);

    Payment createPayment(Payment payment);

    Payment updatePayment(Long id, Payment payment);

    void deletePayment(Long id);

    // Stripe payment confirmation
    Payment confirmPayment(Long orderId, String paymentIntentId, String paymentMethod);

    // Cash on Delivery
    Payment createCODPayment(Long orderId);
}
