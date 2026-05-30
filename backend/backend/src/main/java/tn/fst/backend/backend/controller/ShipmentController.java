package tn.fst.backend.backend.controller;

import tn.fst.backend.backend.entity.Shipment;
import tn.fst.backend.backend.entity.Order;
import tn.fst.backend.backend.repository.ShipmentRepository;
import tn.fst.backend.backend.repository.OrderRepository;
import tn.fst.backend.backend.repository.TransporteurRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shipments")
@CrossOrigin(origins = "http://localhost:4200")
public class ShipmentController {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TransporteurRepository transporteurRepository;

    @GetMapping
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipmentById(@PathVariable Long id) {
        Optional<Shipment> shipment = shipmentRepository.findById(id);
        return shipment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Créer un nouvel envoi
    @PostMapping
    public ResponseEntity<Shipment> createShipment(@RequestBody Shipment shipment) {
        if (shipment.getOrder() != null) {
            Optional<Order> order = orderRepository.findById(shipment.getOrder().getIdOrder());
            if (order.isPresent()) {
                shipment.setOrder(order.get());
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }

        Shipment savedShipment = shipmentRepository.save(shipment);
        return ResponseEntity.ok(savedShipment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Shipment> updateShipment(@PathVariable Long id, @RequestBody Shipment shipmentDetails) {
        Optional<Shipment> optionalShipment = shipmentRepository.findById(id);
        if (!optionalShipment.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Shipment shipment = optionalShipment.get();
        shipment.setCarrier(shipmentDetails.getCarrier());
        shipment.setTrackingNumber(shipmentDetails.getTrackingNumber());
        shipment.setStatus(shipmentDetails.getStatus());
        shipment.setDateShip(shipmentDetails.getDateShip());
        shipment.setShippingMethod(shipmentDetails.getShippingMethod());
        shipment.setShippingCost(shipmentDetails.getShippingCost());
        shipment.setEstimatedDeliveryDate(shipmentDetails.getEstimatedDeliveryDate());
        shipment.setDeliveredAt(shipmentDetails.getDeliveredAt());

        if (shipmentDetails.getOrder() != null) {
            Optional<Order> order = orderRepository.findById(shipmentDetails.getOrder().getIdOrder());
            order.ifPresent(shipment::setOrder);
        }

        // Link Transporter changes back to the Order
        if (shipmentDetails.getTransporterId() != null && shipment.getOrder() != null) {
            Optional<tn.fst.backend.backend.entity.Transporteur> transporteur = transporteurRepository.findById(shipmentDetails.getTransporterId());
            if (transporteur.isPresent()) {
                shipment.getOrder().setTransporteur(transporteur.get());
                orderRepository.save(shipment.getOrder());
            }
        }

        Shipment updatedShipment = shipmentRepository.save(shipment);
        return ResponseEntity.ok(updatedShipment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        if (!shipmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        shipmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
