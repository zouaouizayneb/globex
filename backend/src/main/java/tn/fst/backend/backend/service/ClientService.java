package tn.fst.backend.backend.service;

import tn.fst.backend.backend.entity.Client;
import java.util.List;
import java.util.Optional;

public interface ClientService {
    List<Client> getAllClients();
    Optional<Client> getClientById(Long id);
    Client createClient(Client client);
    Client updateClient(Long id, Client client);
    void deleteClient(Long id);
}

