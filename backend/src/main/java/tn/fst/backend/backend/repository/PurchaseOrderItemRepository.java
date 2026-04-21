package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.ProductVariant;
import tn.fst.backend.backend.entity.PurchaseOrder;
import tn.fst.backend.backend.entity.PurchaseOrderItem;

import java.util.List;
@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {


    List<PurchaseOrderItem> findByPurchaseOrder(PurchaseOrder purchaseOrder);

    List<PurchaseOrderItem> findByVariant(ProductVariant variant);
}
