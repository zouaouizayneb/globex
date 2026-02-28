package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.AddressRequest;
import tn.fst.backend.backend.dto.AddressResponse;
import tn.fst.backend.backend.entity.Address;
import tn.fst.backend.backend.entity.AddressType;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.AddressRepository;
import tn.fst.backend.backend.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(Long userId) {
        User user = getUserById(userId);
        return addressRepository.findByUser(user).stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long userId, Long addressId) {
        User user = getUserById(userId);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (!address.getUser().getIdUser().equals(user.getIdUser())) {
            throw new IllegalArgumentException("Address does not belong to this user");
        }

        return mapToAddressResponse(address);
    }

    public AddressResponse createAddress(Long userId, AddressRequest request) {
        User user = getUserById(userId);

        List<Address> existingAddresses = addressRepository.findByUser(user);
        boolean shouldBeDefault = request.getIsDefault() || existingAddresses.isEmpty();

        if (shouldBeDefault) {
            existingAddresses.forEach(addr -> {
                if (addr.getIsDefault()) {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                }
            });
        }

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(shouldBeDefault)
                .type(AddressType.valueOf(request.getType() != null ? request.getType() : "SHIPPING"))
                .build();

        address = addressRepository.save(address);
        return mapToAddressResponse(address);
    }

    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        User user = getUserById(userId);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (!address.getUser().getIdUser().equals(user.getIdUser())) {
            throw new IllegalArgumentException("Address does not belong to this user");
        }

        address.setFullName(request.getFullName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());

        if (request.getIsDefault() && !address.getIsDefault()) {
            setAsDefault(user, addressId);
        }

        address = addressRepository.save(address);
        return mapToAddressResponse(address);
    }

    public void deleteAddress(Long userId, Long addressId) {
        User user = getUserById(userId);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (!address.getUser().getIdUser().equals(user.getIdUser())) {
            throw new IllegalArgumentException("Address does not belong to this user");
        }

        addressRepository.delete(address);
    }

    public AddressResponse setAsDefault(Long userId, Long addressId) {
        User user = getUserById(userId);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (!address.getUser().getIdUser().equals(user.getIdUser())) {
            throw new IllegalArgumentException("Address does not belong to this user");
        }

        List<Address> userAddresses = addressRepository.findByUser(user);
        userAddresses.forEach(addr -> {
            if (addr.getIsDefault() && !addr.getIdAddress().equals(addressId)) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        });

        address.setIsDefault(true);
        address = addressRepository.save(address);

        return mapToAddressResponse(address);
    }

    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddress(Long userId) {
        User user = getUserById(userId);
        Address address = addressRepository.findByUserAndIsDefaultTrue(user)
                .orElseThrow(() -> new ResourceNotFoundException("No default address found"));
        return mapToAddressResponse(address);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .idAddress(address.getIdAddress())
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .type(address.getType().name())
                .formattedAddress(address.getFormattedAddress())
                .build();
    }
}