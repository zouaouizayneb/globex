package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.ClientRequest;
import tn.fst.backend.backend.dto.ClientResponse;
import tn.fst.backend.backend.entity.Client;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.ClientRepository;
import tn.fst.backend.backend.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        List<Client> clients = clientRepository.findAll();
        return clients.stream()
                .filter(client -> client.getIsDeleted() == null || !client.getIsDeleted())
                .map(this::mapToClientResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
        return mapToClientResponse(client);
    }

    @Override
    public ClientResponse createClient(ClientRequest request) {
        // Create or find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .fullname(request.getName())
                            .username(request.getEmail())
                            .email(request.getEmail())
                            .password("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi") // Default password: "password123"
                            .phoneNumber(request.getPhone())
                            .role(tn.fst.backend.backend.entity.Role.CLIENT)
                            .isActive(true)
                            .build();
                    return userRepository.save(newUser);
                });

        Client client = Client.builder()
                .user(user)
                .name(request.getName())
                .phoneNumber(request.getPhone())
                .address(request.getAddress())
                .country(request.getCountry())
                .orders(new ArrayList<>())
                .build();

        client = clientRepository.save(client);
        return mapToClientResponse(client);
    }

    @Override
    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        // Update client fields
        client.setName(request.getName());
        client.setPhoneNumber(request.getPhone());
        client.setAddress(request.getAddress());
        client.setCountry(request.getCountry());

        // Update associated user if exists
        if (client.getUser() != null) {
            User user = client.getUser();
            user.setFullname(request.getName());
            user.setEmail(request.getEmail());
            user.setPhoneNumber(request.getPhone());
            userRepository.save(user);
        }

        client = clientRepository.save(client);
        return mapToClientResponse(client);
    }

    @Override
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
        
        // Use soft delete to avoid foreign key constraint issues
        client.setIsDeleted(true);
        clientRepository.save(client);
    }

    @Override
    public ClientResponse updateClientStatus(Long id, String status) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        if (client.getUser() != null) {
            User user = client.getUser();
            user.setIsActive("active".equalsIgnoreCase(status));
            userRepository.save(user);
        }

        return mapToClientResponse(client);
    }

    private ClientResponse mapToClientResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getIdClient())
                .name(client.getName())
                .email(client.getUser() != null ? client.getUser().getEmail() : "")
                .phone(client.getPhoneNumber())
                .country(client.getCountry())
                .address(client.getAddress())
                .status(client.getUser() != null && client.getUser().getIsActive() ? "active" : "inactive")
                .totalOrders(0)
                .totalSpent(0.0)
                .joinDate(client.getUser() != null ? client.getUser().getCreatedAt().toString() : "")
                .build();
    }
}
