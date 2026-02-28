package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.PurchaseOrder;
import tn.fst.backend.backend.entity.PurchaseOrderStatus;
import tn.fst.backend.backend.entity.Supplier;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {


    boolean existsByOrderNumber(String orderNumber);


    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    List<PurchaseOrder> findAllByOrderByOrderDateDesc();

    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);

    List<PurchaseOrder> findBySupplier(Supplier supplier);


    List<PurchaseOrder> findBySupplierAndStatus(Supplier supplier, PurchaseOrderStatus status);


    List<PurchaseOrder> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);


    List<PurchaseOrder> findBySupplierAndOrderDateBetweenAndStatus(
            Supplier supplier,
            LocalDate startDate,
            LocalDate endDate,
            PurchaseOrderStatus status
    );


    Long countBySupplier(Supplier supplier);

    Long countByStatus(PurchaseOrderStatus status);
}
