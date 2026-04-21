package tn.fst.backend.backend.entity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ship")
    private Long idShip;

    private String carrier;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "date_ship")
    private LocalDate dateShip = LocalDate.now();

    // Relation ManyToOne avec Order
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public enum Status {
        PREPARING,
        SHIPPED,
        DELIVERED
    }

    public Shipment() {}

    // Getters & Setters
    public Long getIdShip() { return idShip; }
    public void setIdShip(Long idShip) { this.idShip = idShip; }

    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDate getDateShip() { return dateShip; }
    public void setDateShip(LocalDate dateShip) { this.dateShip = dateShip; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}
