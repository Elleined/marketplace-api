package com.elleined.marketplaceapi.service.atm.machine.transaction;

import com.elleined.marketplaceapi.model.atm.transaction.PeerToPeerTransaction;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.atm.PeerToPeerTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PeerToPeerTransactionServiceImpl implements PeerToPeerTransactionService {

    private final PeerToPeerTransactionRepository peerToPeerTransactionRepository;

    @Override
    public PeerToPeerTransaction save(PeerToPeerTransaction peerToPeerTransaction) {
        peerToPeerTransactionRepository.save(peerToPeerTransaction);
        log.debug("Peer to peer transaction saved successfully with trn of {}", peerToPeerTransaction.getTrn());
        return peerToPeerTransaction;
    }

    @Override
    public List<PeerToPeerTransaction> getAllReceivedTransaction(User currentUser) {
        return currentUser.getReceiveMoneyTransactions().stream()
                .sorted(Comparator.comparing(PeerToPeerTransaction::getTransactionDate).reversed())
                .toList();
    }

    @Override
    public List<PeerToPeerTransaction> getAllSentTransactions(User currentUser) {
        return currentUser.getSentMoneyTransactions().stream()
                .sorted(Comparator.comparing(PeerToPeerTransaction::getTransactionDate).reversed())
                .toList();
    }
}
