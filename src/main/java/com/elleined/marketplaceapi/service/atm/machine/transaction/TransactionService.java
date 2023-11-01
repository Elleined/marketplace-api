package com.elleined.marketplaceapi.service.atm.machine.transaction;

import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.model.atm.transaction.Transaction;
import com.elleined.marketplaceapi.model.user.User;

import java.util.Collection;
import java.util.List;

public interface TransactionService<ENTITY extends Transaction> {
    ENTITY save(ENTITY entity);
    ENTITY getById(int id) throws ResourceNotFoundException;
    List<ENTITY> getAll(Collection<Integer> ids);
    List<ENTITY> getAll(User currentUser);
}
