package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.AccountingEntry;

@Repository
public interface AccountingEntryRepository extends JpaRepository<AccountingEntry, Long> {
}
