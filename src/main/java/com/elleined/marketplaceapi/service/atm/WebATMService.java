package com.elleined.marketplaceapi.service.atm;

import com.elleined.marketplaceapi.exception.atm.*;
import com.elleined.marketplaceapi.exception.atm.limit.DepositLimitException;
import com.elleined.marketplaceapi.exception.atm.limit.WithdrawLimitException;
import com.elleined.marketplaceapi.exception.field.MobileNumberException;
import com.elleined.marketplaceapi.model.atm.transaction.DepositTransaction;
import com.elleined.marketplaceapi.model.atm.transaction.PeerToPeerTransaction;
import com.elleined.marketplaceapi.model.atm.transaction.WithdrawTransaction;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.atm.TransactionRepository;
import com.elleined.marketplaceapi.service.atm.machine.DepositService;
import com.elleined.marketplaceapi.service.atm.machine.PeerToPeerService;
import com.elleined.marketplaceapi.service.atm.machine.WithdrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
@Primary
public class WebATMService implements ATMService {
    private final TransactionRepository transactionRepository;

    private final DepositService depositService;
    private final WithdrawService withdrawService;
    private final PeerToPeerService peerToPeerService;

    @Override
    public DepositTransaction requestDeposit(User currentUser, BigDecimal depositAmount, MultipartFile proofOfTransaction) throws NotValidAmountException, DepositLimitException, MalformedProofOfTransaction, IOException {
        // Add validation here
        return depositService.requestDeposit(currentUser, depositAmount, proofOfTransaction);
    }

    @Override
    public WithdrawTransaction requestWithdraw(User currentUser, BigDecimal withdrawnAmount, String gcashNumber)
            throws InsufficientFundException, NotValidAmountException, MinimumAmountException, WithdrawLimitException, MobileNumberException {

        return withdrawService.requestWithdraw(currentUser, withdrawnAmount, gcashNumber);
    }

    @Override
    public PeerToPeerTransaction peerToPeer(User sender, User receiver, BigDecimal sentAmount) throws SendingToHimselfException, InsufficientFundException, NotValidAmountException {
        return peerToPeerService.peerToPeer(sender, receiver, sentAmount);
    }
}
