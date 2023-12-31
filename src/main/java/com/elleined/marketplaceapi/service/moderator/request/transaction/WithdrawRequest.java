package com.elleined.marketplaceapi.service.moderator.request.transaction;

import com.elleined.marketplaceapi.model.Moderator;
import com.elleined.marketplaceapi.model.atm.transaction.Transaction;
import com.elleined.marketplaceapi.model.atm.transaction.WithdrawTransaction;
import com.elleined.marketplaceapi.model.user.Premium;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.ModeratorRepository;
import com.elleined.marketplaceapi.repository.PremiumRepository;
import com.elleined.marketplaceapi.repository.UserRepository;
import com.elleined.marketplaceapi.repository.atm.WithdrawTransactionRepository;
import com.elleined.marketplaceapi.service.atm.machine.withdraw.WithdrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WithdrawRequest implements TransactionRequest<WithdrawTransaction> {
    private final UserRepository userRepository;
    private final PremiumRepository premiumRepository;

    private final ModeratorRepository moderatorRepository;

    private final WithdrawService withdrawService;
    private final WithdrawTransactionRepository withdrawTransactionRepository;

    @Override
    public List<WithdrawTransaction> getAllRequest() {
        List<WithdrawTransaction> premiumUsersWithdrawRequest = premiumRepository.findAll().stream()
                .map(Premium::getUser)
                .map(User::getWithdrawTransactions)
                .flatMap(Collection::stream)
                .filter(withdrawTransaction -> withdrawTransaction.getStatus() == Transaction.Status.PENDING)
                .sorted(Comparator.comparing(WithdrawTransaction::getTransactionDate).reversed())
                .toList();

        List<WithdrawTransaction> regularUsersWithdrawRequest = userRepository.findAll().stream()
                .filter(user -> !user.isPremiumAndNotExpired())
                .map(User::getWithdrawTransactions)
                .flatMap(Collection::stream)
                .filter(withdrawTransaction -> withdrawTransaction.getStatus() == Transaction.Status.PENDING)
                .sorted(Comparator.comparing(WithdrawTransaction::getTransactionDate).reversed())
                .toList();

        List<WithdrawTransaction> withdrawTransactions = new ArrayList<>();
        withdrawTransactions.addAll(premiumUsersWithdrawRequest);
        withdrawTransactions.addAll(regularUsersWithdrawRequest);
        return withdrawTransactions;
    }

    @Override
    public void accept(Moderator moderator, WithdrawTransaction withdrawTransaction) {
        withdrawService.withdraw(withdrawTransaction.getUser(), withdrawTransaction.getAmount());

        withdrawTransaction.setStatus(Transaction.Status.RELEASE);
        moderator.addReleaseWithdrawRequest(withdrawTransaction);

        withdrawTransactionRepository.save(withdrawTransaction);
        moderatorRepository.save(moderator);

        log.debug("Transaction with id of {} are now set to release", withdrawTransaction.getId());
    }

    @Override
    public void acceptAll(Moderator moderator, Set<WithdrawTransaction> withdrawTransactions) {
        withdrawTransactions.forEach(w -> {
            withdrawService.withdraw(w.getUser(), w.getAmount());
            w.setStatus(Transaction.Status.RELEASE);
        });
        moderator.getReleaseWithdrawRequests().addAll(withdrawTransactions);

        moderatorRepository.save(moderator);
        withdrawTransactionRepository.saveAll(withdrawTransactions);

        log.debug("Transactions with ids of {} are now set to release", withdrawTransactions.stream().map(Transaction::getId).toList());
    }

    @Override
    public void reject(Moderator moderator, WithdrawTransaction withdrawTransaction) {
        withdrawTransaction.setStatus(Transaction.Status.REJECTED);
        moderator.addRejectedWithdrawRequest(withdrawTransaction);

        moderatorRepository.save(moderator);
        withdrawTransactionRepository.save(withdrawTransaction);

        log.debug("Transaction with id of {} are now set to rejected", withdrawTransaction.getId());
    }

    @Override
    public void rejectAll(Moderator moderator, Set<WithdrawTransaction> withdrawTransactions) {
        withdrawTransactions.forEach(w -> w.setStatus(Transaction.Status.REJECTED));
        moderator.getRejectedWithdrawRequests().addAll(withdrawTransactions);

        moderatorRepository.save(moderator);
        withdrawTransactionRepository.saveAll(withdrawTransactions);

        log.debug("Transactions with ids of {} are now set to rejected", withdrawTransactions.stream().map(Transaction::getId).toList());
    }
}
