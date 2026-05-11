package tn.fst.backend.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.SupplierStatus;
import tn.fst.backend.backend.service.SupplierService;

import java.util.List;


@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody SupplierRequest request,
            Authentication authentication) {

        SupplierResponse response = supplierService.createSupplier(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllActiveSuppliers() {
        List<SupplierResponse> suppliers = supplierService.getAllActiveSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        List<SupplierResponse> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{supplierId}")
    public ResponseEntity<SupplierResponse> getSupplier(@PathVariable Long supplierId) {
        SupplierResponse supplier = supplierService.getSupplier(supplierId);
        return ResponseEntity.ok(supplier);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SupplierResponse>> searchSuppliers(
            @RequestParam String q) {

        List<SupplierResponse> suppliers = supplierService.searchSuppliers(q);
        return ResponseEntity.ok(suppliers);
    }

    @PutMapping("/{supplierId}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long supplierId,
            @Valid @RequestBody SupplierRequest request,
            Authentication authentication) {

        SupplierResponse response = supplierService.updateSupplier(supplierId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{supplierId}/status")
    public ResponseEntity<SupplierResponse> updateSupplierStatus(
            @PathVariable Long supplierId,
            @RequestParam SupplierStatus status,
            Authentication authentication) {

        SupplierResponse response = supplierService.updateSupplierStatus(supplierId, status);
        return ResponseEntity.ok(response);
    }
}
