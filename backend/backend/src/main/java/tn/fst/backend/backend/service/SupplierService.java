package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.*;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierResponse createSupplier(SupplierRequest request) {
        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .status(SupplierStatus.ACTIVE)
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
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());

        supplier = supplierRepository.save(supplier);
        return mapToSupplierResponse(supplier);
    }

    public SupplierResponse updateSupplierStatus(Long supplierId, SupplierStatus status) {
        Supplier supplier = getSupplierById(supplierId);
        supplier.setStatus(status);
        supplier = supplierRepository.save(supplier);
        return mapToSupplierResponse(supplier);
    }

    private Supplier getSupplierById(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", supplierId));
    }

    private SupplierResponse mapToSupplierResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .supplierId(supplier.getIdSupplier())
                .name(supplier.getName())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .status(supplier.getStatus().name())
                .build();
    }
}