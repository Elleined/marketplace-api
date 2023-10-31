package com.elleined.marketplaceapi.service.atm;

import com.elleined.marketplaceapi.exception.atm.*;
import com.elleined.marketplaceapi.exception.atm.limit.DepositLimitException;
import com.elleined.marketplaceapi.exception.atm.limit.WithdrawLimitException;
import com.elleined.marketplaceapi.exception.field.MobileNumberException;
import com.elleined.marketplaceapi.model.atm.transaction.DepositTransaction;
import com.elleined.marketplaceapi.model.atm.transaction.PeerToPeerTransaction;
import com.elleined.marketplaceapi.model.atm.transaction.WithdrawTransaction;
import com.elleined.marketplaceapi.model.user.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

public interface ATMService {

    DepositTransaction requestDeposit(User currentUser, BigDecimal depositAmount, MultipartFile proofOfTransaction)
            throws NotValidAmountException,
            DepositLimitException,
            MalformedProofOfTransaction, IOException;

    WithdrawTransaction requestWithdraw(User currentUser, BigDecimal withdrawnAmount, String gcashNumber)
            throws InsufficientFundException,
            NotValidAmountException,
            MinimumAmountException,
            WithdrawLimitException,
            MobileNumberException;

    PeerToPeerTransaction peerToPeer(User sender, User receiver, BigDecimal sentAmount)
            throws SendingToHimselfException,
            InsufficientFundException,
            NotValidAmountException;
}
