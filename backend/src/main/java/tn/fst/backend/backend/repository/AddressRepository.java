package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // Find all addresses for a user
    List<Address> findByUser(User user);

    // Find user's default address
    Optional<Address> findByUserAndIsDefaultTrue(User user);

    // Find addresses by type
    List<Address> findByUserAndType(User user, AddressType type);

    // Find by country (for analytics)
    List<Address> findByCountry(String country);
}
