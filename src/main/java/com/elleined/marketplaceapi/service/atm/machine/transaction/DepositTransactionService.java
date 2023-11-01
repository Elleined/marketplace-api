package com.elleined.marketplaceapi.service.atm.machine.transaction;

import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.model.atm.transaction.DepositTransaction;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.atm.DepositTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DepositTransactionService implements TransactionService<DepositTransaction> {
    private final DepositTransactionRepository depositTransactionRepository;


    @Override
    public DepositTransaction save(DepositTransaction depositTransaction) {

        depositTransactionRepository.save(depositTransaction);
        log.debug("Deposit transaction saved with trn of {}", depositTransaction.getTrn());
        return depositTransaction;
    }

    @Override
    public DepositTransaction getById(int id) throws ResourceNotFoundException {
        return depositTransactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction with id of " + id + " doesn't exists!"));
    }

    @Override
    public List<DepositTransaction> getAll(Collection<Integer> ids) {
        return depositTransactionRepository.findAllById(ids);
    }

    @Override
    public List<DepositTransaction> getAll(User currentUser) {
        return currentUser.getDepositTransactions().stream()
                .sorted(Comparator.comparing(DepositTransaction::getTransactionDate).reversed())
                .toList();
    }
}
