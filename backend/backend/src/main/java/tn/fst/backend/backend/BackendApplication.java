package tn.fst.backend.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tn.fst.backend.backend.entity.Client;
import tn.fst.backend.backend.entity.Role;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.repository.ClientRepository;
import tn.fst.backend.backend.repository.UserRepository;

import java.util.List;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner synchronizeClients(UserRepository userRepository, ClientRepository clientRepository) {
        return args -> {
            List<User> users = userRepository.findAll();
            List<Client> clients = clientRepository.findAll();
            
            for (User user : users) {
                if (user.getRole() == Role.CLIENT) {
                    boolean hasClient = clients.stream()
                        .anyMatch(c -> c.getUser() != null && c.getUser().getIdUser().equals(user.getIdUser()));
                    if (!hasClient) {
                        System.out.println("Migrating missing client for user ID " + user.getIdUser());
                        Client client = new Client();
                        client.setUser(user);
                        client.setName(user.getFullname());
                        client.setPhoneNumber(user.getPhoneNumber());
                        clientRepository.save(client);
                    }
                }
            }
        };
    }
}

