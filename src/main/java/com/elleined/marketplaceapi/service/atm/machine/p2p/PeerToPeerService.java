package com.elleined.marketplaceapi.service.atm.machine.p2p;

import com.elleined.marketplaceapi.exception.atm.InsufficientFundException;
import com.elleined.marketplaceapi.exception.atm.NotValidAmountException;
import com.elleined.marketplaceapi.exception.atm.SendingToHimselfException;
import com.elleined.marketplaceapi.exception.atm.limit.LimitException;
import com.elleined.marketplaceapi.exception.atm.limit.PeerToPeerLimitPerDayException;
import com.elleined.marketplaceapi.model.atm.transaction.PeerToPeerTransaction;
import com.elleined.marketplaceapi.model.atm.transaction.Transaction;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.UserRepository;
import com.elleined.marketplaceapi.service.AppWalletService;
import com.elleined.marketplaceapi.service.atm.fee.ATMFeeService;
import com.elleined.marketplaceapi.service.atm.machine.TransactionService;
import com.elleined.marketplaceapi.service.atm.machine.validator.ATMLimitPerDayValidator;
import com.elleined.marketplaceapi.service.atm.machine.validator.ATMValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PeerToPeerService implements ATMLimitPerDayValidator {
    public static final int PEER_TO_PEER_LIMIT_PER_DAY = 10_000;
    public static final int MAXIMUM_AMOUNT = 10_000;
    public static final int MINIMUM_AMOUNT = 500;

    private final UserRepository userRepository;
    private final ATMValidator atmValidator;
    private final ATMFeeService feeService;
    private final P2PTransactionService p2PTransactionService;
    private final AppWalletService appWalletService;

    public PeerToPeerTransaction peerToPeer(User sender, User receiver, BigDecimal sentAmount)
            throws SendingToHimselfException,
            InsufficientFundException,
            NotValidAmountException,
            LimitException {

        if (isSenderSendingToHimself(sender, receiver)) throw new SendingToHimselfException("You cannot send to yourself");
        if (atmValidator.isNotValidAmount(sentAmount)) throw new NotValidAmountException("Cannot send money! because amount should be positive and cannot be zero!");
        if (sender.isBalanceNotEnough(sentAmount)) throw new InsufficientFundException("Insufficient Funds!");
        if (reachedLimitAmountPerDay(sender)) throw new PeerToPeerLimitPerDayException("Cannot send money! Because you already reached the sent amount limit per day which is " + PEER_TO_PEER_LIMIT_PER_DAY);
        if (atmValidator.isUserTotalPendingRequestAmountAboveBalance(sender, sentAmount)) throw new InsufficientFundException("Cannot send money! because you're balance cannot be less than in you're total pending withdraw request. Cancel some of your withdraw request or wait for our team to settle you withdraw request.");

        float p2pFee = feeService.getP2pFee(sentAmount);
        BigDecimal finalSentAmount = sentAmount.subtract(new BigDecimal(p2pFee));
        BigDecimal senderOldBalance = sender.getBalance();
        BigDecimal receiverOldBalance = receiver.getBalance();

        updateSenderBalance(sender, sentAmount);
        updateRecipientBalance(receiver, finalSentAmount);
        appWalletService.addAndSaveBalance(p2pFee);
        PeerToPeerTransaction peerToPeerTransaction = p2PTransactionService.save(sender, receiver, sentAmount);

        log.debug("Sender with id of {} sent money amounting {} from {} because of p2p fee of {} which is the {}% of sent amount.", sender.getId(), finalSentAmount, sentAmount, p2pFee, ATMFeeService.P2P_FEE_PERCENTAGE);
        log.debug("Sender with id of {} has now new balance of {} from {}.", sender.getId(), sender.getBalance(), senderOldBalance);
        log.debug("Receiver with id of {} has now new balance of {} from {}", receiver.getId(), receiver.getBalance(), receiverOldBalance);
        return peerToPeerTransaction;
    }

    private void updateSenderBalance(User sender, BigDecimal amountToBeDeducted) {
        BigDecimal newBalance = sender.getBalance().subtract(amountToBeDeducted);
        sender.setBalance(newBalance);
        userRepository.save(sender);
    }

    private void updateRecipientBalance(User receiver, BigDecimal amountToBeAdded) {
        BigDecimal newBalance = receiver.getBalance().add(amountToBeAdded);
        receiver.setBalance(newBalance);
        userRepository.save(receiver);
    }

    private boolean isSenderSendingToHimself(User sender, User receiver) {
        return sender.getId() == receiver.getId();
    }

    @Override
    public boolean reachedLimitAmountPerDay(User sender) {
        final LocalDateTime currentDateTimeMidnight = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        final LocalDateTime tomorrowMidnight = currentDateTimeMidnight.plusDays(1);
        List<PeerToPeerTransaction> userSentMoneyTransactions = sender.getSentMoneyTransactions();
        List<PeerToPeerTransaction>  sentMoneyTransactions =
                TransactionService.getTransactionsByDateRange(userSentMoneyTransactions, currentDateTimeMidnight, tomorrowMidnight);

        BigDecimal totalSentAmount = sentMoneyTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal::add)
                .orElseGet(() -> new BigDecimal(0));
        int comparisonResult = totalSentAmount.compareTo(new BigDecimal(PEER_TO_PEER_LIMIT_PER_DAY));
        return comparisonResult >= 0;
    }
}
