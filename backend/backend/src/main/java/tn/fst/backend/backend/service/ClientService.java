package tn.fst.backend.backend.service;

import tn.fst.backend.backend.dto.ClientRequest;
import tn.fst.backend.backend.dto.ClientResponse;
import java.util.List;

public interface ClientService {
    List<ClientResponse> getAllClients();
    ClientResponse getClientById(Long id);
    ClientResponse createClient(ClientRequest request);
    ClientResponse updateClient(Long id, ClientRequest request);
    ClientResponse updateClientStatus(Long id, String status);
    void deleteClient(Long id);
}

