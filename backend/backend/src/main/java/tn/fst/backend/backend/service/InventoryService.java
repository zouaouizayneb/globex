package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.*;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final ProductVariantRepository variantRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;
    private static final int DEFAULT_REORDER_POINT = 20;
    private static final int DEFAULT_REORDER_QUANTITY = 50;

    @Transactional(readOnly = true)
    public StockLevelResponse getStockLevel(Long variantId) {
        ProductVariant variant = getVariantById(variantId);

        return StockLevelResponse.builder()
                .variantId(variant.getIdVariant())
                .productName(variant.getProduct().getName())
                .sku(variant.getSku())
                .color(variant.getColor())
                .size(variant.getSize())
                .currentStock(variant.getStockQuantity())
                .lowStockThreshold(DEFAULT_LOW_STOCK_THRESHOLD)
                .reorderPoint(DEFAULT_REORDER_POINT)
                .isLowStock(variant.getStockQuantity() <= DEFAULT_LOW_STOCK_THRESHOLD)
                .needsReorder(variant.getStockQuantity() <= DEFAULT_REORDER_POINT)
                .status(determineStockStatus(variant.getStockQuantity()))
                .build();
    }

    @Transactional(readOnly = true)
    public List<LowStockAlert> getLowStockAlerts() {
        List<ProductVariant> lowStockVariants =
                variantRepository.findByStockQuantityLessThanEqual(DEFAULT_LOW_STOCK_THRESHOLD);

        return lowStockVariants.stream()
                .map(this::mapToLowStockAlert)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReorderAlert> getReorderAlerts() {
        List<ProductVariant> reorderVariants =
                variantRepository.findByStockQuantityLessThanEqual(DEFAULT_REORDER_POINT);

        return reorderVariants.stream()
                .map(this::mapToReorderAlert)
                .collect(Collectors.toList());
    }
    public StockAdjustmentResponse adjustStock(Long variantId, StockAdjustmentRequest request) {
        ProductVariant variant = getVariantById(variantId);

        int stockBefore = variant.getStockQuantity();
        int newStock = stockBefore + request.getQuantityChange();

        // Vérifier que le stock ne devient pas négatif
        if (newStock < 0) {
            throw new IllegalArgumentException(
                    "Le stock ne peut pas être négatif. Stock actuel: " + stockBefore +
                            ", Changement demandé: " + request.getQuantityChange()
            );
        }

        variant.setStockQuantity(newStock);
        variantRepository.save(variant);

        // Sync with Stock table
        stockRepository.findByVariant(variant).ifPresentOrElse(
            stock -> {
                stock.setQuantity(newStock);
                stockRepository.save(stock);
            },
            () -> {
                Stock newStockEntry = new Stock();
                newStockEntry.setProduct(variant.getProduct());
                newStockEntry.setVariant(variant);
                newStockEntry.setQuantity(newStock);
                stockRepository.save(newStockEntry);
            }
        );

        StockMovement movement = StockMovement.builder()
                .variant(variant)
                .type(MovementType.ADJUSTMENT)
                .quantity(request.getQuantityChange())
                .stockBefore(stockBefore)
                .stockAfter(newStock)
                .reason(request.getReason())
                .build();
        stockMovementRepository.save(movement);

        return StockAdjustmentResponse.builder()
                .success(true)
                .message("Stock ajusté avec succès")
                .variantId(variantId)
                .stockBefore(stockBefore)
                .stockAfter(newStock)
                .quantityChanged(request.getQuantityChange())
                .build();
    }

    public void recordSale(Long variantId, Integer quantity, Long orderId) {
        ProductVariant variant = getVariantById(variantId);

        int stockBefore = variant.getStockQuantity();
        int stockAfter = Math.max(0, stockBefore - quantity);

        variant.setStockQuantity(stockAfter);
        variantRepository.save(variant);

        // Sync with Stock table
        stockRepository.findByVariant(variant).ifPresent(stock -> {
            stock.setQuantity(stockAfter);
            stockRepository.save(stock);
        });

        StockMovement movement = StockMovement.builder()
                .variant(variant)
                .type(MovementType.SALE)
                .quantity(-quantity)
                .stockBefore(stockBefore)
                .stockAfter(stockAfter)
                .referenceId(orderId)
                .referenceType("ORDER")
                .reason("Vente - Commande #" + orderId)
                .build();
        stockMovementRepository.save(movement);
    }

    public void recordReturn(Long variantId, Integer quantity, Long orderId) {
        ProductVariant variant = getVariantById(variantId);

        int stockBefore = variant.getStockQuantity();
        int stockAfter = stockBefore + quantity;

        variant.setStockQuantity(stockAfter);
        variantRepository.save(variant);

        // Sync with Stock table
        stockRepository.findByVariant(variant).ifPresent(stock -> {
            stock.setQuantity(stockAfter);
            stockRepository.save(stock);
        });

        StockMovement movement = StockMovement.builder()
                .variant(variant)
                .type(MovementType.RETURN)
                .quantity(quantity)
                .stockBefore(stockBefore)
                .stockAfter(stockAfter)
                .referenceId(orderId)
                .referenceType("ORDER")
                .reason("Retour - Commande #" + orderId)
                .build();
        stockMovementRepository.save(movement);
    }

    public StockAdjustmentResponse recordPurchase(Long variantId, Integer quantity, String supplier) {
        ProductVariant variant = getVariantById(variantId);

        int stockBefore = variant.getStockQuantity();
        int stockAfter = stockBefore + quantity;

        variant.setStockQuantity(stockAfter);
        variantRepository.save(variant);

        // Sync with Stock table
        stockRepository.findByVariant(variant).ifPresentOrElse(
            stock -> {
                stock.setQuantity(stockAfter);
                stockRepository.save(stock);
            },
            () -> {
                Stock newStockEntry = new Stock();
                newStockEntry.setProduct(variant.getProduct());
                newStockEntry.setVariant(variant);
                newStockEntry.setQuantity(stockAfter);
                stockRepository.save(newStockEntry);
            }
        );

        StockMovement movement = StockMovement.builder()
                .variant(variant)
                .type(MovementType.PURCHASE)
                .quantity(quantity)
                .stockBefore(stockBefore)
                .stockAfter(stockAfter)
                .reason("Réapprovisionnement - Fournisseur: " + supplier)
                .build();
        stockMovementRepository.save(movement);

        return StockAdjustmentResponse.builder()
                .success(true)
                .message("Réapprovisionnement enregistré avec succès")
                .variantId(variantId)
                .stockBefore(stockBefore)
                .stockAfter(stockAfter)
                .quantityChanged(quantity)
                .build();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockHistory(Long variantId) {
        ProductVariant variant = getVariantById(variantId);
        List<StockMovement> movements = stockMovementRepository
                .findByVariantOrderByCreatedAtDesc(variant);

        return movements.stream()
                .map(this::mapToStockMovementResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InventoryReportResponse getInventoryReport() {
        List<ProductVariant> allVariants = variantRepository.findAll();

        long totalVariants = allVariants.size();
        long inStock = allVariants.stream()
                .filter(v -> v.getStockQuantity() > 0)
                .count();
        long outOfStock = allVariants.stream()
                .filter(v -> v.getStockQuantity() == 0)
                .count();
        long lowStock = allVariants.stream()
                .filter(v -> v.getStockQuantity() > 0 &&
                        v.getStockQuantity() <= DEFAULT_LOW_STOCK_THRESHOLD)
                .count();
        long needsReorder = allVariants.stream()
                .filter(v -> v.getStockQuantity() <= DEFAULT_REORDER_POINT)
                .count();

        int totalStockValue = allVariants.stream()
                .mapToInt(ProductVariant::getStockQuantity)
                .sum();

        return InventoryReportResponse.builder()
                .totalVariants(totalVariants)
                .inStock(inStock)
                .outOfStock(outOfStock)
                .lowStock(lowStock)
                .needsReorder(needsReorder)
                .totalStockUnits(totalStockValue)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public List<StockLevelResponse> getOutOfStockVariants() {
        List<ProductVariant> outOfStock = variantRepository.findByStockQuantity(0);

        return outOfStock.stream()
                .map(variant -> StockLevelResponse.builder()
                        .variantId(variant.getIdVariant())
                        .productName(variant.getProduct().getName())
                        .sku(variant.getSku())
                        .color(variant.getColor())
                        .size(variant.getSize())
                        .currentStock(0)
                        .status("OUT_OF_STOCK")
                        .isLowStock(true)
                        .needsReorder(true)
                        .build())
                .collect(Collectors.toList());
    }
    private ProductVariant getVariantById(Long variantId) {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantId));
    }

    private String determineStockStatus(Integer stock) {
        if (stock == 0) {
            return "OUT_OF_STOCK";
        } else if (stock <= DEFAULT_LOW_STOCK_THRESHOLD) {
            return "LOW_STOCK";
        } else if (stock <= DEFAULT_REORDER_POINT) {
            return "REORDER_NEEDED";
        } else {
            return "IN_STOCK";
        }
    }

    private LowStockAlert mapToLowStockAlert(ProductVariant variant) {
        return LowStockAlert.builder()
                .variantId(variant.getIdVariant())
                .productName(variant.getProduct().getName())
                .sku(variant.getSku())
                .color(variant.getColor())
                .size(variant.getSize())
                .currentStock(variant.getStockQuantity())
                .threshold(DEFAULT_LOW_STOCK_THRESHOLD)
                .severity(variant.getStockQuantity() == 0 ? "CRITICAL" : "WARNING")
                .message(variant.getStockQuantity() == 0 ?
                        "Rupture de stock" :
                        "Stock faible (" + variant.getStockQuantity() + " restants)")
                .build();
    }

    private ReorderAlert mapToReorderAlert(ProductVariant variant) {
        return ReorderAlert.builder()
                .variantId(variant.getIdVariant())
                .productName(variant.getProduct().getName())
                .sku(variant.getSku())
                .color(variant.getColor())
                .size(variant.getSize())
                .currentStock(variant.getStockQuantity())
                .reorderPoint(DEFAULT_REORDER_POINT)
                .suggestedOrderQuantity(DEFAULT_REORDER_QUANTITY)
                .priority(variant.getStockQuantity() == 0 ? "HIGH" : "MEDIUM")
                .build();
    }

    private StockMovementResponse mapToStockMovementResponse(StockMovement movement) {
        return StockMovementResponse.builder()
                .movementId(movement.getIdMovement())
                .type(movement.getType().name())
                .quantity(movement.getQuantity())
                .stockBefore(movement.getStockBefore())
                .stockAfter(movement.getStockAfter())
                .referenceType(movement.getReferenceType())
                .referenceId(movement.getReferenceId())
                .reason(movement.getReason())
                .createdAt(movement.getCreatedAt())
                .build();
    }
}
