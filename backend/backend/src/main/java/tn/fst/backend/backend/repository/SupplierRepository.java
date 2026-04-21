package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {


    boolean existsByCode(String code);


    Optional<Supplier> findByCode(String code);

    List<Supplier> findByStatus(SupplierStatus status);


    List<Supplier> findByCountry(String country);


    @Query("SELECT s FROM Supplier s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Supplier> searchSuppliers(@Param("keyword") String keyword);


    @Query("SELECT s FROM Supplier s WHERE s.status = 'ACTIVE' " +
            "ORDER BY s.rating DESC")
    List<Supplier> findTopByRating(int limit);
}

