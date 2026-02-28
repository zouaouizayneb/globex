package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.Order;
import tn.fst.backend.backend.entity.Client;
import tn.fst.backend.backend.entity.OrderStatus;
import tn.fst.backend.backend.entity.User;



import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>,
        JpaSpecificationExecutor<Order> {

    List<Order> findByClient(Client client);
    List<Order> findByDateOrderBetween(LocalDate startDate, LocalDate endDate);
    List<Order> findByClientAndStatus(Client client, OrderStatus status);
    List<Order> findByClient_IdClient(Long clientId);
    List<Order> findByUserOrderByDateOrderDesc(User user);
    List<Order> findByUserAndStatus(User user, OrderStatus status);
    List<Order> findByUserAndDateOrderBetween(User user, LocalDate startDate, LocalDate endDate);
    Long countByUser(User user);
    Long countByStatus(OrderStatus status);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByDateOrderBetweenAndStatus(
            LocalDateTime startDate,
            LocalDateTime endDate,
            OrderStatus status
    );
}