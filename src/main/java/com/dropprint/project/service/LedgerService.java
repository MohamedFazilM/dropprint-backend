package com.dropprint.project.service;

import com.dropprint.project.model.Ledger;
import com.dropprint.project.repository.LedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LedgerService {

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private IdGeneratorService idGeneratorService;

    public void log(String entityType, String entityId, String action, Double amount, String description) {
        Ledger entry = new Ledger();
        entry.setId(idGeneratorService.generate("ldg", "ledger_id_seq"));
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setAction(action);
        entry.setAmount(amount);
        entry.setDescription(description);
        ledgerRepository.save(entry);
    }
}