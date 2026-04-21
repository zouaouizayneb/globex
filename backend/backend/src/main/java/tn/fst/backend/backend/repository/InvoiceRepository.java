package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.Invoice;
import tn.fst.backend.backend.entity.InvoiceStatus;
import tn.fst.backend.backend.entity.Order;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    boolean existsByOrder(Order order);

    Optional<Invoice> findByOrder(Order order);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    List<Invoice> findAllByOrderByIssueDateDesc();

    List<Invoice> findByUser_IdUserOrderByIssueDateDesc(Long userId);


    List<Invoice> findByStatus(InvoiceStatus status);


    List<Invoice> findByIssueDateBetween(LocalDate startDate, LocalDate endDate);


    List<Invoice> findByStatusAndPaidDateBetween(
            InvoiceStatus status,
            LocalDate startDate,
            LocalDate endDate
    );

    Long countByStatus(InvoiceStatus status);

    Long countByUser_IdUser(Long userId);
}
