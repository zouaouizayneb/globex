package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.*;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryService inventoryService;

    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {

        Supplier supplier = getSupplierById(request.getSupplierId());

        String orderNumber = generateOrderNumber();

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .orderNumber(orderNumber)
                .supplier(supplier)
                .status(PurchaseOrderStatus.DRAFT)
                .orderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .shippingMethod(request.getShippingMethod())
                .warehouseLocation(request.getWarehouseLocation())
                .notes(request.getNotes())
                .currency(request.getCurrency() != null ? request.getCurrency() : "TND")
                .items(new ArrayList<>())
                .build();

        for (PurchaseOrderItemRequest itemReq : request.getItems()) {
            ProductVariant variant = getVariantById(itemReq.getVariantId());

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .variant(variant)
                    .quantity(itemReq.getQuantity())
                    .unitCost(itemReq.getUnitCost())
                    .notes(itemReq.getNotes())
                    .build();

            purchaseOrder.addItem(item);
        }

        purchaseOrder.setTaxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO);
        purchaseOrder.setShippingCost(request.getShippingCost() != null ? request.getShippingCost() : BigDecimal.ZERO);
        purchaseOrder.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        purchaseOrder.calculateTotals();

        if (supplier.getPaymentTerms() != null) {
            purchaseOrder.setPaymentDueDate(
                    purchaseOrder.getOrderDate().plusDays(supplier.getPaymentTerms())
            );
        }

        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        return mapToPurchaseOrderResponse(purchaseOrder);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrder(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(purchaseOrderId);
        return mapToPurchaseOrderResponse(purchaseOrder);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAllPurchaseOrders() {
        List<PurchaseOrder> orders = purchaseOrderRepository.findAllByOrderByOrderDateDesc();
        return orders.stream()
                .map(this::mapToPurchaseOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getPurchaseOrdersByStatus(PurchaseOrderStatus status) {
        List<PurchaseOrder> orders = purchaseOrderRepository.findByStatus(status);
        return orders.stream()
                .map(this::mapToPurchaseOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getPurchaseOrdersBySupplier(Long supplierId) {
        Supplier supplier = getSupplierById(supplierId);
        List<PurchaseOrder> orders = purchaseOrderRepository.findBySupplier(supplier);
        return orders.stream()
                .map(this::mapToPurchaseOrderResponse)
                .collect(Collectors.toList());
    }

    public PurchaseOrderResponse updatePurchaseOrder(Long purchaseOrderId, PurchaseOrderRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(purchaseOrderId);

        if (!purchaseOrder.canBeModified()) {
            throw new IllegalStateException("Ce bon de commande ne peut plus être modifié");
        }

        purchaseOrder.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        purchaseOrder.setShippingMethod(request.getShippingMethod());
        purchaseOrder.setWarehouseLocation(request.getWarehouseLocation());
        purchaseOrder.setNotes(request.getNotes());
        purchaseOrder.setTaxAmount(request.getTaxAmount());
        purchaseOrder.setShippingCost(request.getShippingCost());
        purchaseOrder.setDiscountAmount(request.getDiscountAmount());

        purchaseOrder.calculateTotals();

        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        return mapToPurchaseOrderResponse(purchaseOrder);
    }

    public PurchaseOrderResponse submitPurchaseOrder(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(purchaseOrderId);

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException("Seuls les brouillons peuvent être envoyés");
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING);
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        // TODO: Envoyer email au fournisseur

        return mapToPurchaseOrderResponse(purchaseOrder);
    }

    public PurchaseOrderResponse confirmPurchaseOrder(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(purchaseOrderId);

        purchaseOrder.setStatus(PurchaseOrderStatus.CONFIRMED);
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        return mapToPurchaseOrderResponse(purchaseOrder);
    }

    public PurchaseOrderResponse markAsShipped(Long purchaseOrderId, String trackingNumber) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(purchaseOrderId);

        purchaseOrder.setStatus(PurchaseOrderStatus.SHIPPED);
        purchaseOrder.setTrackingNumber(trackingNumber);
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        return mapToPurchaseOrderResponse(purchaseOrder);
    }

    public PurchaseOrderResponse receivePurchaseOrder(Long purchaseOrderId, ReceivePurchaseOrderRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(purchaseOrderId);

        for (ReceivedItemRequest receivedItem : request.getReceivedItems()) {
            PurchaseOrderItem item = purchaseOrder.getItems().stream()
                    .filter(i -> i.getIdItem().equals(receivedItem.getItemId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrderItem", receivedItem.getItemId()));

            item.setReceivedQuantity(receivedItem.getReceivedQuantity());
            item.setDamagedQuantity(receivedItem.getDamagedQuantity());

            int goodQuantity = receivedItem.getReceivedQuantity() - receivedItem.getDamagedQuantity();
            if (goodQuantity > 0) {
                ProductVariant variant = item.getVariant();
                variant.setStockQuantity(variant.getStockQuantity() + goodQuantity);
                variantRepository.save(variant);

                inventoryService.recordPurchase(
                        variant.getIdVariant(),
                        goodQuantity,
                        purchaseOrder.getSupplier().getName()
                );
            }
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);
        purchaseOrder.setActualDeliveryDate(request.getReceivedDate() != null ?
                request.getReceivedDate() :
                LocalDate.now());

        updateSupplierStatistics(purchaseOrder);

        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        return mapToPurchaseOrderResponse(purchaseOrder);
    }

    public PurchaseOrderResponse markAsPaid(Long purchaseOrderId, PaymentInfoRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(purchaseOrderId);

        purchaseOrder.setPaymentMethod(request.getPaymentMethod());
        purchaseOrder.setPaymentReference(request.getPaymentReference());
        purchaseOrder.setPaidDate(request.getPaymentDate() != null ?
                request.getPaymentDate() :
                LocalDate.now());
        purchaseOrder.setStatus(PurchaseOrderStatus.COMPLETED);

        Supplier supplier = purchaseOrder.getSupplier();
        supplier.setTotalSpent(supplier.getTotalSpent().add(purchaseOrder.getTotalAmount()));
        supplierRepository.save(supplier);

        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        return mapToPurchaseOrderResponse(purchaseOrder);
    }

    public PurchaseOrderResponse cancelPurchaseOrder(Long purchaseOrderId, String reason) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(purchaseOrderId);

        if (!purchaseOrder.canBeCancelled()) {
            throw new IllegalStateException("Ce bon de commande ne peut pas être annulé");
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);
        purchaseOrder.setInternalNotes(
                (purchaseOrder.getInternalNotes() != null ? purchaseOrder.getInternalNotes() + "\n" : "") +
                        "ANNULÉ: " + reason
        );

        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        return mapToPurchaseOrderResponse(purchaseOrder);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getLatePurchaseOrders() {
        List<PurchaseOrder> orders = purchaseOrderRepository.findAll();
        return orders.stream()
                .filter(PurchaseOrder::isLate)
                .map(this::mapToPurchaseOrderResponse)
                .collect(Collectors.toList());
    }


    private PurchaseOrder getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
    }

    private Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }

    private ProductVariant getVariantById(Long id) {
        return variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", id));
    }

    private String generateOrderNumber() {
        long count = purchaseOrderRepository.count();
        int year = LocalDate.now().getYear();
        return String.format("PO-%d-%04d", year, count + 1);
    }

    private void updateSupplierStatistics(PurchaseOrder purchaseOrder) {
        Supplier supplier = purchaseOrder.getSupplier();

        supplier.setTotalOrders(supplier.getTotalOrders() + 1);

        if (purchaseOrder.getActualDeliveryDate() != null &&
                purchaseOrder.getExpectedDeliveryDate() != null) {

            if (!purchaseOrder.getActualDeliveryDate().isAfter(purchaseOrder.getExpectedDeliveryDate())) {
                supplier.setOnTimeDeliveries(supplier.getOnTimeDeliveries() + 1);
            } else {
                supplier.setLateDeliveries(supplier.getLateDeliveries() + 1);
            }
        }

        if (supplier.getFirstOrderDate() == null) {
            supplier.setFirstOrderDate(purchaseOrder.getOrderDate());
        }
        supplier.setLastOrderDate(LocalDate.now());

        supplierRepository.save(supplier);
    }

    private PurchaseOrderResponse mapToPurchaseOrderResponse(PurchaseOrder po) {
        List<PurchaseOrderItemResponse> itemResponses = po.getItems().stream()
                .map(item -> PurchaseOrderItemResponse.builder()
                        .itemId(item.getIdItem())
                        .variantId(item.getVariant().getIdVariant())
                        .productName(item.getVariant().getProduct().getName())
                        .sku(item.getVariant().getSku())
                        .color(item.getVariant().getColor())
                        .size(item.getVariant().getSize())
                        .quantity(item.getQuantity())
                        .unitCost(item.getUnitCost())
                        .lineTotal(item.getLineTotal())
                        .receivedQuantity(item.getReceivedQuantity())
                        .damagedQuantity(item.getDamagedQuantity())
                        .remainingQuantity(item.getRemainingQuantity())
                        .notes(item.getNotes())
                        .build())
                .collect(Collectors.toList());

        return PurchaseOrderResponse.builder()
                .purchaseOrderId(po.getIdPurchaseOrder())
                .orderNumber(po.getOrderNumber())
                .supplierId(po.getSupplier().getIdSupplier())
                .supplierName(po.getSupplier().getName())
                .status(po.getStatus().name())
                .orderDate(po.getOrderDate())
                .expectedDeliveryDate(po.getExpectedDeliveryDate())
                .actualDeliveryDate(po.getActualDeliveryDate())
                .paymentDueDate(po.getPaymentDueDate())
                .paidDate(po.getPaidDate())
                .subtotal(po.getSubtotal())
                .taxAmount(po.getTaxAmount())
                .shippingCost(po.getShippingCost())
                .discountAmount(po.getDiscountAmount())
                .totalAmount(po.getTotalAmount())
                .currency(po.getCurrency())
                .items(itemResponses)
                .paymentMethod(po.getPaymentMethod())
                .paymentReference(po.getPaymentReference())
                .shippingMethod(po.getShippingMethod())
                .trackingNumber(po.getTrackingNumber())
                .warehouseLocation(po.getWarehouseLocation())
                .notes(po.getNotes())
                .internalNotes(po.getInternalNotes())
                .isLate(po.isLate())
                .canBeModified(po.canBeModified())
                .canBeCancelled(po.canBeCancelled())
                .createdAt(po.getCreatedAt())
                .build();
    }
}