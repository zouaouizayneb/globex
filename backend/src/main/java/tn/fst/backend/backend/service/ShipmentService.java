package tn.fst.backend.backend.service;

import tn.fst.backend.backend.entity.Shipment;
import java.util.List;
import java.util.Optional;

public interface ShipmentService {
    List<Shipment> getAllShipments();
    Optional<Shipment> getShipmentById(Long id);
    Shipment createShipment(Shipment shipment);
    Shipment updateShipment(Long id, Shipment shipment);
    void deleteShipment(Long id);
}
