package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<Order> findByDateOrderBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByClientAndStatus(Client client, OrderStatus status);
    List<Order> findByClient_IdClient(Long clientId);
    List<Order> findByUserOrderByDateOrderDesc(User user);
    List<Order> findByUserAndStatus(User user, OrderStatus status);
    List<Order> findByUserAndDateOrderBetween(User user, LocalDate startDate, LocalDate endDate);
    Long countByUser(User user);
    Long countByStatus(OrderStatus status);
    List<Order> findByStatus(OrderStatus status);
    @EntityGraph(attributePaths = {"orderDetails", "orderDetails.variant", "orderDetails.variant.product"})
    List<Order> findByDateOrderBetweenAndStatus(
            LocalDate startDate,
            LocalDate endDate,
            OrderStatus status
    );

    @EntityGraph(attributePaths = {"orderDetails", "orderDetails.variant", "orderDetails.variant.product"})
    List<Order> findByDateOrderBetweenAndStatusIn(
            LocalDate startDate,
            LocalDate endDate,
            List<OrderStatus> statuses
    );

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails od LEFT JOIN FETCH od.variant v LEFT JOIN FETCH v.product p WHERE o.dateOrder BETWEEN :start AND :end")
    List<Order> findAllWithDetailsBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails od LEFT JOIN FETCH od.variant v LEFT JOIN FETCH v.product p WHERE o.dateOrder BETWEEN :start AND :end AND o.status = :status")
    List<Order> findAllWithDetailsBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("status") OrderStatus status
    );
}