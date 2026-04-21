package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.UserResponse;
import tn.fst.backend.backend.dto.UserUpdateRequest;
import tn.fst.backend.backend.entity.Client;
import tn.fst.backend.backend.entity.Role;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.ClientRepository;
import tn.fst.backend.backend.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword != null && !rawPassword.isBlank() ? rawPassword : "changeme"));
        User saved = userRepository.save(user);

        if (saved.getRole() == Role.CLIENT) {
            Client client = new Client();
            client.setUser(saved);
            client.setName(saved.getFullname());
            client.setPhoneNumber(saved.getPhoneNumber());
            clientRepository.save(client);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (request.getFullname() != null) user.setFullname(request.getFullname());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getIsActive() != null) user.setIsActive(request.getIsActive());
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getIdUser())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public int syncClients() {
        java.util.List<User> users = userRepository.findAll();
        java.util.List<Client> clients = clientRepository.findAll();
        int count = 0;
        for (User user : users) {
            if (user.getRole() == Role.CLIENT) {
                boolean hasClient = clients.stream()
                        .anyMatch(c -> c.getUser() != null && c.getUser().getIdUser().equals(user.getIdUser()));
                if (!hasClient) {
                    Client client = new Client();
                    client.setUser(user);
                    client.setName(user.getFullname());
                    client.setPhoneNumber(user.getPhoneNumber());
                    clientRepository.save(client);
                    count++;
                }
            }
        }
        return count;
    }
}
