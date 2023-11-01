package com.elleined.marketplaceapi.service.atm.machine.transaction;

import com.elleined.marketplaceapi.model.atm.transaction.PeerToPeerTransaction;
import com.elleined.marketplaceapi.model.user.User;

import java.util.List;

public interface PeerToPeerTransactionService {
    PeerToPeerTransaction save(PeerToPeerTransaction peerToPeerTransaction);
    List<PeerToPeerTransaction> getAllReceivedTransaction(User currentUser);
    List<PeerToPeerTransaction> getAllSentTransactions(User currentUser);
}
