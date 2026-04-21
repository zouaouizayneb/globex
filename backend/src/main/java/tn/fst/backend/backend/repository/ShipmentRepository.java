package tn.fst.backend.backend.repository;

import tn.fst.backend.backend.entity.Order;
import tn.fst.backend.backend.entity.OrderStatus;
import tn.fst.backend.backend.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // Find shipment by order
    Optional<Shipment> findByOrder(Order order);

    // Find by tracking number
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    // Find all shipments by status
    List<Shipment> findByStatus(OrderStatus status);

    // Find shipments by carrier
    List<Shipment> findByCarrier(String carrier);

    // Find shipments to a specific country
    List<Shipment> findByShippingAddress_Country(String country);
}