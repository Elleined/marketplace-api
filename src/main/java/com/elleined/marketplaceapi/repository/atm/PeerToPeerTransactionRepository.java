package com.elleined.marketplaceapi.repository.atm;

import com.elleined.marketplaceapi.model.atm.transaction.PeerToPeerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeerToPeerTransactionRepository extends JpaRepository<PeerToPeerTransaction, Integer> {
}