package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.Transporteur;

@Repository
public interface TransporteurRepository extends JpaRepository<Transporteur, Long> {
}
