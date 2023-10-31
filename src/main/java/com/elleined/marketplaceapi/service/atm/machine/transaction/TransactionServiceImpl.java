package com.elleined.marketplaceapi.service.atm.machine.transaction;

import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.model.atm.transaction.DepositTransaction;
import com.elleined.marketplaceapi.model.atm.transaction.PeerToPeerTransaction;
import com.elleined.marketplaceapi.model.atm.transaction.Transaction;
import com.elleined.marketplaceapi.model.atm.transaction.WithdrawTransaction;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.atm.DepositTransactionRepository;
import com.elleined.marketplaceapi.repository.atm.TransactionRepository;
import com.elleined.marketplaceapi.repository.atm.WithdrawTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final WithdrawTransactionRepository withdrawTransactionRepository;
    private final DepositTransactionRepository depositTransactionRepository;

    @Override
    public Transaction save(Transaction atmTransaction) {
        return transactionRepository.save(atmTransaction);
    }

    @Override
    public Transaction getById(int id) throws ResourceNotFoundException {
        return transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction with id of " + id + " doesn't exists!"));
    }

    @Override
    public WithdrawTransaction getWithdrawTransactionById(int withdrawTransactionId) {
        return withdrawTransactionRepository.findById(withdrawTransactionId).orElseThrow(() -> new ResourceNotFoundException("Withdraw transaction with id of " + withdrawTransactionId + " doesn't exists!"));
    }

    @Override
    public DepositTransaction getDepositTransactionById(int depositTransactionId) throws ResourceNotFoundException {
        return depositTransactionRepository.findById(depositTransactionId).orElseThrow(() -> new ResourceNotFoundException("Transaction with id of " + depositTransactionId + " doesn't exists!"));
    }

    @Override
    public List<DepositTransaction> getAllDepositTransactionById(Set<Integer> depositTransactionIds) {
        return depositTransactionRepository.findAllById(depositTransactionIds);
    }

    @Override
    public List<WithdrawTransaction> getAllWithdrawTransactions(Set<Integer> withdrawTransactionIds) {
        return withdrawTransactionRepository.findAllById(withdrawTransactionIds);
    }

    @Override
    public List<WithdrawTransaction> getAllWithdrawalTransactions(User currentUser) {
        return currentUser.getWithdrawTransactions().stream()
                .sorted(Comparator.comparing(WithdrawTransaction::getTransactionDate).reversed())
                .toList();
    }

    @Override
    public List<DepositTransaction> getAllDepositTransactions(User currentUser) {
        return currentUser.getDepositTransactions().stream()
                .sorted(Comparator.comparing(DepositTransaction::getTransactionDate).reversed())
                .toList();
    }

    @Override
    public List<PeerToPeerTransaction> getAllReceiveMoneyTransactions(User currentUser) {
        return currentUser.getReceiveMoneyTransactions().stream()
                .sorted(Comparator.comparing(PeerToPeerTransaction::getTransactionDate).reversed())
                .toList();
    }

    @Override
    public List<PeerToPeerTransaction> getAllSentMoneyTransactions(User currentUser) {
        return currentUser.getSentMoneyTransactions().stream()
                .sorted(Comparator.comparing(PeerToPeerTransaction::getTransactionDate).reversed())
                .toList();
    }
}
