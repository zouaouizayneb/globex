package tn.fst.backend.backend.service;

import tn.fst.backend.backend.entity.Client;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.repository.ClientRepository;
import tn.fst.backend.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Override
    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    @Override
    public Client createClient(Client client) {
        // Vérifier que l'utilisateur associé existe
        if (client.getUser() != null) {
            Optional<User> user = userRepository.findById(client.getUser().getIdUser());
            user.ifPresent(client::setUser);
        } else {
            throw new RuntimeException("User is required for Client");
        }

        return clientRepository.save(client);
    }

    @Override
    public Client updateClient(Long id, Client clientDetails) {
        Optional<Client> optionalClient = clientRepository.findById(id);
        if (!optionalClient.isPresent()) {
            throw new RuntimeException("Client not found with id: " + id);
        }

        Client client = optionalClient.get();
        client.setAddress(clientDetails.getAddress());
        client.setCountry(clientDetails.getCountry());

        // Mettre à jour l'utilisateur si fourni
        if (clientDetails.getUser() != null) {
            Optional<User> user = userRepository.findById(clientDetails.getUser().getIdUser());
            user.ifPresent(client::setUser);
        }

        return clientRepository.save(client);
    }

    @Override
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new RuntimeException("Client not found with id: " + id);
        }
        clientRepository.deleteById(id);
    }
}
