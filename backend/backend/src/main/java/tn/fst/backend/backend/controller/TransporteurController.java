package tn.fst.backend.backend.controller;

import tn.fst.backend.backend.entity.Transporteur;
import tn.fst.backend.backend.repository.TransporteurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transporteurs")
@CrossOrigin(origins = "http://localhost:4200")
public class TransporteurController {

    @Autowired
    private TransporteurRepository transporteurRepository;

    @GetMapping
    public List<Transporteur> getAllTransporteurs() {
        return transporteurRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transporteur> getTransporteurById(@PathVariable Long id) {
        Optional<Transporteur> t = transporteurRepository.findById(id);
        return t.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Transporteur createTransporteur(@RequestBody Transporteur transporteur) {
        return transporteurRepository.save(transporteur);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transporteur> updateTransporteur(@PathVariable Long id, @RequestBody Transporteur details) {
        return transporteurRepository.findById(id).map(t -> {
            t.setName(details.getName());
            t.setEmail(details.getEmail());
            t.setPhone(details.getPhone());
            t.setAddress(details.getAddress());
            t.setStatus(details.getStatus());
            t.setDeliveryFee(details.getDeliveryFee());
            return ResponseEntity.ok(transporteurRepository.save(t));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransporteur(@PathVariable Long id) {
        if (!transporteurRepository.existsById(id)) return ResponseEntity.notFound().build();
        transporteurRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
