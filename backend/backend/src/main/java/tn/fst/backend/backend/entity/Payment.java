package tn.fst.backend.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_payment")
    private Long idPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "mode_payment")
    private String modePayment;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "date_payment")
    private LocalDate datePayment;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    private String currency;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "payment_intent_id")
    private String paymentIntentId;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @PrePersist
    protected void onCreate() {
        if (datePayment == null) {
            datePayment = LocalDate.now();
        }
    }

    /**
     * Check if the payment was successful
     */
    public boolean isSuccessful() {
        return status == PaymentStatus.COMPLETED;
    }

    /**
     * Alias for backward compatibility — getModePayment
     */
    public String getModePayment() {
        if (modePayment != null) {
            return modePayment;
        }
        return paymentMethod != null ? paymentMethod.name() : null;
    }

    public void setModePayment(String modePayment) {
        this.modePayment = modePayment;
    }
}
