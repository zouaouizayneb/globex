package tn.fst.backend.backend.controller;

import tn.fst.backend.backend.entity.Client;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.repository.ClientRepository;
import tn.fst.backend.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "http://localhost:4200")
public class ClientController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        Optional<Client> client = clientRepository.findById(id);
        return client.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        // Vérifier que l'utilisateur existe
        if (client.getUser() != null) {
            Optional<User> user = userRepository.findById(client.getUser().getIdUser());
            if (user.isPresent()) {
                client.setUser(user.get());
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }

        Client savedClient = clientRepository.save(client);
        return ResponseEntity.ok(savedClient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @RequestBody Client clientDetails) {
        Optional<Client> optionalClient = clientRepository.findById(id);
        if (!optionalClient.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Client client = optionalClient.get();
        client.setAddress(clientDetails.getAddress());
        client.setCountry(clientDetails.getCountry());

        // Mettre à jour l'utilisateur si fourni
        if (clientDetails.getUser() != null) {
            Optional<User> user = userRepository.findById(clientDetails.getUser().getIdUser());
            user.ifPresent(client::setUser);
        }

        Client updatedClient = clientRepository.save(client);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        if (!clientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        clientRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
