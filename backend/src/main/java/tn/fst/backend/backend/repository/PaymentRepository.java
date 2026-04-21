package tn.fst.backend.backend.repository;

import tn.fst.backend.backend.entity.Order;
import tn.fst.backend.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.PaymentMethod;
import tn.fst.backend.backend.entity.PaymentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payment by order
    Optional<Payment> findByOrder(Order order);

    // Find by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);

    // Find by payment intent ID (Stripe)
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

    // Find all payments by status
    List<Payment> findByStatus(PaymentStatus status);

    // Find all payments by method
    List<Payment> findByPaymentMethod(PaymentMethod method);
}
