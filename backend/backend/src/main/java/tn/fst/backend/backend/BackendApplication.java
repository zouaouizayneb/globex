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
    public CommandLineRunner synchronizeClients(UserRepository userRepository, ClientRepository clientRepository, org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE invoices MODIFY COLUMN user_id BIGINT NULL;");
                System.out.println("Successfully altered invoices table to make user_id nullable.");
            } catch (Exception e) {
                System.out.println("Could not alter invoices table: " + e.getMessage());
            }

            // Fix orphaned stock entries where product_id is NULL but variant_id is present
            try {
                jdbcTemplate.execute(
                    "UPDATE stock s " +
                    "JOIN product_variants pv ON s.variant_id = pv.id_variant " +
                    "SET s.product_id = pv.product_id " +
                    "WHERE s.product_id IS NULL AND s.variant_id IS NOT NULL"
                );
                System.out.println("Successfully fixed orphaned stock entries.");
            } catch (Exception e) {
                System.out.println("Could not fix stock table: " + e.getMessage());
            }
            
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

