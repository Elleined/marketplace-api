package com.elleined.marketplaceapi.service.moderator.request.transaction;

import com.elleined.marketplaceapi.model.Moderator;
import com.elleined.marketplaceapi.model.atm.transaction.DepositTransaction;
import com.elleined.marketplaceapi.model.atm.transaction.Transaction;
import com.elleined.marketplaceapi.model.user.Premium;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.ModeratorRepository;
import com.elleined.marketplaceapi.repository.PremiumRepository;
import com.elleined.marketplaceapi.repository.UserRepository;
import com.elleined.marketplaceapi.repository.atm.DepositTransactionRepository;
import com.elleined.marketplaceapi.service.atm.machine.deposit.DepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DepositRequest implements TransactionRequest<DepositTransaction> {
    private final UserRepository userRepository;
    private final PremiumRepository premiumRepository;

    private final DepositTransactionRepository depositTransactionRepository;
    private final DepositService depositService;

    private final ModeratorRepository moderatorRepository;

    @Override
    public List<DepositTransaction> getAllRequest() {
        List<DepositTransaction> premiumUserDepositRequests = premiumRepository.findAll().stream()
                .map(Premium::getUser)
                .map(User::getDepositTransactions)
                .flatMap(Collection::stream)
                .filter(DepositTransaction::isPending)
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .toList();

        List<DepositTransaction> regularUserDepositRequests = userRepository.findAll().stream()
                .filter(user -> !user.isPremiumAndNotExpired())
                .map(User::getDepositTransactions)
                .flatMap(Collection::stream)
                .filter(DepositTransaction::isPending)
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .toList();

        List<DepositTransaction> depositTransactions = new ArrayList<>();
        depositTransactions.addAll(premiumUserDepositRequests);
        depositTransactions.addAll(regularUserDepositRequests);
        return depositTransactions;
    }

    @Override
    public void accept(Moderator moderator, DepositTransaction depositTransaction) {
        depositService.deposit(depositTransaction.getUser(), depositTransaction.getAmount());

        depositTransaction.setStatus(Transaction.Status.RELEASE);
        moderator.addReleaseDepositRequest(depositTransaction);

        depositTransactionRepository.save(depositTransaction);
        moderatorRepository.save(moderator);

        log.debug("Transaction with id of {} are now release", depositTransaction.getId());
    }

    @Override
    public void acceptAll(Moderator moderator, Set<DepositTransaction> depositTransactions) {
        depositTransactions.forEach(d -> {
            depositService.deposit(d.getUser(), d.getAmount());
            d.setStatus(Transaction.Status.RELEASE);
        });

        moderator.getReleaseDepositRequest().addAll(depositTransactions);

        moderatorRepository.save(moderator);
        depositTransactionRepository.saveAll(depositTransactions);

        log.debug("Transactions with ids of {} are now set to release", depositTransactions.stream().map(Transaction::getId).toList());
    }

    @Override
    public void reject(Moderator moderator, DepositTransaction depositTransaction) {
        depositTransaction.setStatus(Transaction.Status.REJECTED);
        moderator.addRejectedDepositRequest(depositTransaction);

        depositTransactionRepository.save(depositTransaction);
        moderatorRepository.save(moderator);

        log.debug("Transaction with id of {} are now set to rejected", depositTransaction.getId());
    }

    @Override
    public void rejectAll(Moderator moderator, Set<DepositTransaction> depositTransactions) {
        depositTransactions.forEach(d -> d.setStatus(Transaction.Status.REJECTED));
        moderator.getRejectedDepositRequest().addAll(depositTransactions);

        moderatorRepository.save(moderator);
        depositTransactionRepository.saveAll(depositTransactions);

        log.debug("Transactions with ids of {} are now set to rejected", depositTransactions.stream().map(Transaction::getId).toList());
    }
}
