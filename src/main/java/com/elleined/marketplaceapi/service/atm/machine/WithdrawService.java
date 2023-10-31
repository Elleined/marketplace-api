package com.elleined.marketplaceapi.service.atm.machine;


import com.elleined.marketplaceapi.exception.atm.InsufficientFundException;
import com.elleined.marketplaceapi.exception.atm.MinimumAmountException;
import com.elleined.marketplaceapi.exception.atm.NotValidAmountException;
import com.elleined.marketplaceapi.exception.atm.limit.WithdrawLimitException;
import com.elleined.marketplaceapi.exception.atm.limit.WithdrawLimitPerDayException;
import com.elleined.marketplaceapi.exception.field.MobileNumberException;
import com.elleined.marketplaceapi.model.atm.transaction.Transaction;
import com.elleined.marketplaceapi.model.atm.transaction.WithdrawTransaction;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.UserRepository;
import com.elleined.marketplaceapi.repository.atm.WithdrawTransactionRepository;
import com.elleined.marketplaceapi.service.AppWalletService;
import com.elleined.marketplaceapi.service.atm.fee.ATMFeeService;
import com.elleined.marketplaceapi.service.atm.machine.validator.ATMLimitPerDayValidator;
import com.elleined.marketplaceapi.service.atm.machine.validator.ATMLimitValidator;
import com.elleined.marketplaceapi.service.atm.machine.validator.ATMValidator;
import com.elleined.marketplaceapi.service.validator.NumberValidator;
import com.elleined.marketplaceapi.utils.TransactionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WithdrawService implements ATMLimitValidator, ATMLimitPerDayValidator {
    public static final int WITHDRAWAL_LIMIT_PER_DAY = 10_000;
    public static final int MAXIMUM_WITHDRAW_AMOUNT = 10_000;
    public static final int MINIMUM_WITHDRAW_AMOUNT = 500;

    private final UserRepository userRepository;

    private final ATMValidator atmValidator;
    private final NumberValidator numberValidator;

    private final WithdrawTransactionRepository withdrawTransactionRepository;

    private final ATMFeeService feeService;
    private final AppWalletService appWalletService;

    public void withdraw(User currentUser, @NonNull BigDecimal withdrawalAmount) {
        BigDecimal oldBalance = currentUser.getBalance();
        float withdrawalFee = feeService.getWithdrawalFee(withdrawalAmount);
        BigDecimal finalWithdrawalAmount = withdrawalAmount.subtract(new BigDecimal(withdrawalFee));
        currentUser.setBalance(oldBalance.subtract(withdrawalAmount));
        userRepository.save(currentUser);
        appWalletService.addAndSaveBalance(withdrawalFee);

        log.debug("User with id of {} withdraw amounting {} from {} because of withdrawal fee of {} which is the {}% of withdrawn amount and has new balance of {} from {}", currentUser.getId(), finalWithdrawalAmount, withdrawalAmount, withdrawalFee, ATMFeeService.WITHDRAWAL_FEE_PERCENTAGE, currentUser.getBalance(), oldBalance);
    }

    public WithdrawTransaction requestWithdraw(@NonNull User user, @NonNull BigDecimal withdrawalAmount, String gcashNumber)
            throws InsufficientFundException,
            NotValidAmountException,
            MinimumAmountException,
            WithdrawLimitException,
            MobileNumberException {

        numberValidator.validate(gcashNumber);
        if (atmValidator.isNotValidAmount(withdrawalAmount))
            throw new NotValidAmountException("Cannot withdraw! Because the amount you provided " + withdrawalAmount + " for the withdraw is invalid. Please ensure that the withdraw amount is a positive value greater than zero.");
        if (atmValidator.isBalanceEnough(user, withdrawalAmount))
            throw new InsufficientFundException("Cannot withdraw! Because you do not have a sufficient balance to complete the transaction. Please ensure that your account has enough funds for this transfer.");
        if (atmValidator.isUserTotalPendingRequestAmountAboveBalance(user))
            throw new InsufficientFundException("Cannot withdraw! Because your account balance cannot be less than the total of your pending withdrawal requests. Please cancel some of your withdrawal requests or wait for our team to settle your withdrawal requests.");
        if (isBelowMinimum(withdrawalAmount))
            throw new MinimumAmountException("Cannot withdraw! Because the withdraw amount you entered is below the required minimum withdraw " + MINIMUM_WITHDRAW_AMOUNT + ". Please ensure that your withdraw meets the minimum requirement.");
        if (isAboveMaximum(withdrawalAmount))
            throw new WithdrawLimitException("Cannot withdraw! You cannot make a withdraw because the amount you entered exceeds the maximum withdraw limit which is " + MAXIMUM_WITHDRAW_AMOUNT);
        if (reachedLimitAmountPerDay(user))
            throw new WithdrawLimitPerDayException("Cannot withdraw! Because you have already reached the daily sending limit, which is " + WITHDRAWAL_LIMIT_PER_DAY);

        String trn = UUID.randomUUID().toString();

        WithdrawTransaction withdrawTransaction = WithdrawTransaction.builder()
                .trn(trn)
                .amount(withdrawalAmount)
                .transactionDate(LocalDateTime.now())
                .status(Transaction.Status.PENDING)
                .gcashNumber(gcashNumber)
                .user(user)
                .build();

        withdrawTransactionRepository.save(withdrawTransaction);
        log.debug("Withdraw transaction saved with trn of {}", trn);
        return withdrawTransaction;
    }

    @Override
    public boolean isAboveMaximum(BigDecimal withdrawalAmount) {
        return withdrawalAmount.compareTo(new BigDecimal(MAXIMUM_WITHDRAW_AMOUNT)) > 0;
    }

    @Override
    public boolean isBelowMinimum(BigDecimal amountToBeWithdrawn) {
        return amountToBeWithdrawn.compareTo(new BigDecimal(MINIMUM_WITHDRAW_AMOUNT)) < 0;
    }

    @Override
    public boolean reachedLimitAmountPerDay(User currentUser) {
        final LocalDateTime currentDateTimeMidnight = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        final LocalDateTime tomorrowMidnight = currentDateTimeMidnight.plusDays(1);
        List<WithdrawTransaction> userWithdrawTransactions = currentUser.getWithdrawTransactions();
        List<WithdrawTransaction>  withdrawTransactions =
                TransactionUtils.getTransactionsByDateRange(userWithdrawTransactions, currentDateTimeMidnight, tomorrowMidnight);

        BigDecimal totalWithdrawAmount = withdrawTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal::add)
                .orElseGet(() -> new BigDecimal(0));
        int comparisonResult = totalWithdrawAmount.compareTo(new BigDecimal(WITHDRAWAL_LIMIT_PER_DAY));
        return comparisonResult >= 0;
    }
}
