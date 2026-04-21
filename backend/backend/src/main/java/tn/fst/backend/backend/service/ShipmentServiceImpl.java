package tn.fst.backend.backend.service;

import tn.fst.backend.backend.entity.Shipment;
import tn.fst.backend.backend.entity.Order;
import tn.fst.backend.backend.repository.ShipmentRepository;
import tn.fst.backend.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    @Override
    public Optional<Shipment> getShipmentById(Long id) {
        return shipmentRepository.findById(id);
    }

    @Override
    public Shipment createShipment(Shipment shipment) {
        if (shipment.getOrder() != null) {
            orderRepository.findById(shipment.getOrder().getIdOrder())
                    .ifPresent(shipment::setOrder);
        }
        return shipmentRepository.save(shipment);
    }

    @Override
    public Shipment updateShipment(Long id, Shipment shipmentDetails) {
        Optional<Shipment> optional = shipmentRepository.findById(id);
        if (!optional.isPresent()) throw new RuntimeException("Shipment not found with id: " + id);

        Shipment shipment = optional.get();
        shipment.setCarrier(shipmentDetails.getCarrier());
        shipment.setTrackingNumber(shipmentDetails.getTrackingNumber());
        shipment.setStatus(shipmentDetails.getStatus());
        shipment.setDateShip(shipmentDetails.getDateShip());

        if (shipmentDetails.getOrder() != null)
            orderRepository.findById(shipmentDetails.getOrder().getIdOrder())
                    .ifPresent(shipment::setOrder);

        return shipmentRepository.save(shipment);
    }

    @Override
    public void deleteShipment(Long id) {
        if (!shipmentRepository.existsById(id)) throw new RuntimeException("Shipment not found with id: " + id);
        shipmentRepository.deleteById(id);
    }
}
