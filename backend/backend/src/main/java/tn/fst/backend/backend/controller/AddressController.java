package tn.fst.backend.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.AddressRequest;
import tn.fst.backend.backend.dto.AddressResponse;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.service.AddressService;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * Get all addresses for current user
     * GET /api/addresses
     */
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getUserAddresses(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }

    /**
     * Get specific address
     * GET /api/addresses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getAddress(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        AddressResponse address = addressService.getAddressById(userId, id);
        return ResponseEntity.ok(address);
    }

    /**
     * Get default address
     * GET /api/addresses/default
     */
    @GetMapping("/default")
    public ResponseEntity<AddressResponse> getDefaultAddress(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        AddressResponse address = addressService.getDefaultAddress(userId);
        return ResponseEntity.ok(address);
    }

    /**
     * Create new address
     * POST /api/addresses
     */
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @Valid @RequestBody AddressRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        AddressResponse address = addressService.createAddress(userId, request);
        return new ResponseEntity<>(address, HttpStatus.CREATED);
    }

    /**
     * Update address
     * PUT /api/addresses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        AddressResponse address = addressService.updateAddress(userId, id, request);
        return ResponseEntity.ok(address);
    }

    /**
     * Delete address
     * DELETE /api/addresses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        addressService.deleteAddress(userId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Set address as default
     * POST /api/addresses/{id}/set-default
     */
    @PostMapping("/{id}/set-default")
    public ResponseEntity<AddressResponse> setAsDefault(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        AddressResponse address = addressService.setAsDefault(userId, id);
        return ResponseEntity.ok(address);
    }

    private Long getCurrentUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getIdUser();
    }
}
