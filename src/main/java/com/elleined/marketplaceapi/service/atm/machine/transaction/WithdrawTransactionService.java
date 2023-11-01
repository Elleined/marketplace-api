package com.elleined.marketplaceapi.service.atm.machine.transaction;

import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.model.atm.transaction.WithdrawTransaction;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.atm.WithdrawTransactionRepository;
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
public class WithdrawTransactionService implements NewTransaction<WithdrawTransaction> {
    private final WithdrawTransactionRepository withdrawTransactionRepository;

    @Override
    public WithdrawTransaction save(WithdrawTransaction withdrawTransaction) {
        withdrawTransactionRepository.save(withdrawTransaction);
        log.debug("Withdraw transaction saved with trn of {}", withdrawTransaction.getTrn());
        return withdrawTransaction;
    }

    @Override
    public WithdrawTransaction getById(int id) throws ResourceNotFoundException {
        return withdrawTransactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Withdraw transaction with id of " + id + " doesn't exists!"));
    }

    @Override
    public List<WithdrawTransaction> getAll(List<Integer> ids) {
        return withdrawTransactionRepository.findAllById(ids);
    }

    @Override
    public List<WithdrawTransaction> getAll(User currentUser) {
        return currentUser.getWithdrawTransactions().stream()
                .sorted(Comparator.comparing(WithdrawTransaction::getTransactionDate).reversed())
                .toList();
    }
}
