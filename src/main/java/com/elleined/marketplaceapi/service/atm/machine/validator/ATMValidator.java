package com.elleined.marketplaceapi.service.atm.machine.validator;

import com.elleined.marketplaceapi.model.atm.transaction.WithdrawTransaction;
import com.elleined.marketplaceapi.model.user.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ATMValidator {

    public boolean isBalanceEnough(User user, BigDecimal amountToBeSent) {
        return user.getBalance().compareTo(amountToBeSent) < 0;
    }

    public boolean isNotValidAmount(BigDecimal amount) {
        return amount == null || amount.compareTo(BigDecimal.ZERO) <= 0;
    }

    public boolean isUserTotalPendingRequestAmountAboveBalance(User currentUser) {
        BigDecimal totalPendingAmount = currentUser.getWithdrawTransactions().stream()
                .filter(WithdrawTransaction::isPending)
                .map(WithdrawTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalPendingAmount.compareTo(currentUser.getBalance()) > 0;
    }
}
