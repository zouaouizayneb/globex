package tn.fst.backend.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_payment")
    private Long idPayment;

    @Column(name = "mode_payment")
    private String modePayment;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Getter
    @Column(name = "date_payment")
    private LocalDate datePayment = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;



    public Payment() {}

    public Long getIdPayment() {
        return idPayment;
    }

    public void setIdPayment(Long idPayment) {
        this.idPayment = idPayment;
    }

    public String getPaymentMethod() {
        return modePayment;
    }

    public void setPaymentMethod(String modePayment) {
        this.modePayment = modePayment;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setDatePayment(LocalDate datePayment) {
        this.datePayment = datePayment;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
