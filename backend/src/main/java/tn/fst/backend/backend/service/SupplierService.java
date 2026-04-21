package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.*;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public SupplierResponse createSupplier(SupplierRequest request) {

        String code = request.getCode();
        if (code == null || code.isEmpty()) {
            code = generateSupplierCode(request.getName());
        }

        if (supplierRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Un fournisseur avec ce code existe déjà");
        }

        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .code(code)
                .description(request.getDescription())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .mobile(request.getMobile())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .taxId(request.getTaxId())
                .website(request.getWebsite())
                .paymentTerms(request.getPaymentTerms())
                .leadTime(request.getLeadTime())
                .minimumOrder(request.getMinimumOrder())
                .status(SupplierStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        supplier = supplierRepository.save(supplier);
        return mapToSupplierResponse(supplier);
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplier(Long supplierId) {
        Supplier supplier = getSupplierById(supplierId);
        return mapToSupplierResponse(supplier);
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllActiveSuppliers() {
        List<Supplier> suppliers = supplierRepository.findByStatus(SupplierStatus.ACTIVE);
        return suppliers.stream()
                .map(this::mapToSupplierResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        return suppliers.stream()
                .map(this::mapToSupplierResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> searchSuppliers(String keyword) {
        List<Supplier> suppliers = supplierRepository.searchSuppliers(keyword);
        return suppliers.stream()
                .map(this::mapToSupplierResponse)
                .collect(Collectors.toList());
    }

    public SupplierResponse updateSupplier(Long supplierId, SupplierRequest request) {
        Supplier supplier = getSupplierById(supplierId);

        supplier.setName(request.getName());
        supplier.setDescription(request.getDescription());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setMobile(request.getMobile());
        supplier.setAddressLine1(request.getAddressLine1());
        supplier.setAddressLine2(request.getAddressLine2());
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        supplier.setPostalCode(request.getPostalCode());
        supplier.setCountry(request.getCountry());
        supplier.setTaxId(request.getTaxId());
        supplier.setWebsite(request.getWebsite());
        supplier.setPaymentTerms(request.getPaymentTerms());
        supplier.setLeadTime(request.getLeadTime());
        supplier.setMinimumOrder(request.getMinimumOrder());
        supplier.setNotes(request.getNotes());

        supplier = supplierRepository.save(supplier);
        return mapToSupplierResponse(supplier);
    }

    public SupplierResponse updateSupplierStatus(Long supplierId, SupplierStatus status) {
        Supplier supplier = getSupplierById(supplierId);
        supplier.setStatus(status);
        supplier = supplierRepository.save(supplier);
        return mapToSupplierResponse(supplier);
    }

    @Transactional(readOnly = true)
    public SupplierStatisticsResponse getSupplierStatistics(Long supplierId) {
        Supplier supplier = getSupplierById(supplierId);

        List<PurchaseOrder> orders = purchaseOrderRepository.findBySupplier(supplier);

        long totalOrders = orders.size();
        long completedOrders = orders.stream()
                .filter(PurchaseOrder::isCompleted)
                .count();

        long onTimeDeliveries = orders.stream()
                .filter(po -> po.getActualDeliveryDate() != null &&
                        po.getExpectedDeliveryDate() != null &&
                        !po.getActualDeliveryDate().isAfter(po.getExpectedDeliveryDate()))
                .count();

        long lateDeliveries = orders.stream()
                .filter(PurchaseOrder::isLate)
                .count();

        BigDecimal totalSpent = orders.stream()
                .filter(PurchaseOrder::isCompleted)
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double onTimeRate = totalOrders > 0 ? (onTimeDeliveries * 100.0) / totalOrders : 0;

        return SupplierStatisticsResponse.builder()
                .supplierId(supplierId)
                .supplierName(supplier.getName())
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .onTimeDeliveries(onTimeDeliveries)
                .lateDeliveries(lateDeliveries)
                .onTimeDeliveryRate(BigDecimal.valueOf(onTimeRate).setScale(2, RoundingMode.HALF_UP))
                .totalSpent(totalSpent)
                .rating(supplier.getRating())
                .qualityRating(supplier.getQualityRating())
                .deliveryRating(supplier.getDeliveryRating())
                .serviceRating(supplier.getServiceRating())
                .firstOrderDate(supplier.getFirstOrderDate())
                .lastOrderDate(supplier.getLastOrderDate())
                .build();
    }

    public SupplierResponse rateSupplier(Long supplierId, SupplierRatingRequest request) {
        Supplier supplier = getSupplierById(supplierId);

        supplier.setQualityRating(request.getQualityRating());
        supplier.setDeliveryRating(request.getDeliveryRating());
        supplier.setServiceRating(request.getServiceRating());

        BigDecimal avgRating = request.getQualityRating()
                .add(request.getDeliveryRating())
                .add(request.getServiceRating())
                .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);

        supplier.setRating(avgRating);

        supplier = supplierRepository.save(supplier);
        return mapToSupplierResponse(supplier);
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getTopSuppliers(int limit) {
        List<Supplier> suppliers = supplierRepository.findTopByRating(limit);
        return suppliers.stream()
                .map(this::mapToSupplierResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getSuppliersByCountry(String country) {
        List<Supplier> suppliers = supplierRepository.findByCountry(country);
        return suppliers.stream()
                .map(this::mapToSupplierResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SupplierCostReport> getCostReport(LocalDate startDate, LocalDate endDate) {
        List<Supplier> suppliers = supplierRepository.findAll();

        return suppliers.stream()
                .map(supplier -> {
                    List<PurchaseOrder> orders = purchaseOrderRepository
                            .findBySupplierAndOrderDateBetweenAndStatus(
                                    supplier,
                                    startDate,
                                    endDate,
                                    PurchaseOrderStatus.COMPLETED
                            );

                    BigDecimal totalCost = orders.stream()
                            .map(PurchaseOrder::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return SupplierCostReport.builder()
                            .supplierId(supplier.getIdSupplier())
                            .supplierName(supplier.getName())
                            .orderCount(orders.size())
                            .totalCost(totalCost)
                            .currency(orders.isEmpty() ? "TND" : orders.get(0).getCurrency())
                            .build();
                })
                .filter(report -> report.getTotalCost().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getTotalCost().compareTo(a.getTotalCost()))
                .collect(Collectors.toList());
    }


    private Supplier getSupplierById(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", supplierId));
    }

    private String generateSupplierCode(String name) {
        String prefix = name.substring(0, Math.min(3, name.length())).toUpperCase();
        long count = supplierRepository.count();
        return String.format("%s-%04d", prefix, count + 1);
    }

    private SupplierResponse mapToSupplierResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .supplierId(supplier.getIdSupplier())
                .name(supplier.getName())
                .code(supplier.getCode())
                .description(supplier.getDescription())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .mobile(supplier.getMobile())
                .addressLine1(supplier.getAddressLine1())
                .addressLine2(supplier.getAddressLine2())
                .city(supplier.getCity())
                .state(supplier.getState())
                .postalCode(supplier.getPostalCode())
                .country(supplier.getCountry())
                .formattedAddress(supplier.getFormattedAddress())
                .taxId(supplier.getTaxId())
                .website(supplier.getWebsite())
                .paymentTerms(supplier.getPaymentTerms())
                .leadTime(supplier.getLeadTime())
                .minimumOrder(supplier.getMinimumOrder())
                .status(supplier.getStatus().name())
                .rating(supplier.getRating())
                .qualityRating(supplier.getQualityRating())
                .deliveryRating(supplier.getDeliveryRating())
                .serviceRating(supplier.getServiceRating())
                .totalOrders(supplier.getTotalOrders())
                .totalSpent(supplier.getTotalSpent())
                .onTimeDeliveryRate(supplier.getOnTimeDeliveryRate())
                .firstOrderDate(supplier.getFirstOrderDate())
                .lastOrderDate(supplier.getLastOrderDate())
                .notes(supplier.getNotes())
                .createdAt(supplier.getCreatedAt())
                .build();
    }
}