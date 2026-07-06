package com.dropprint.project.repository;

import com.dropprint.project.model.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LedgerRepository extends JpaRepository<Ledger, String> {
    List<Ledger> findByEntityIdOrderByCreatedAtDesc(String entityId);
    List<Ledger> findAllByOrderByCreatedAtDesc();
}