package com.elleined.marketplaceapi.controller.atm;

import com.elleined.marketplaceapi.dto.atm.dto.DepositTransactionDTO;
import com.elleined.marketplaceapi.dto.atm.dto.PeerToPeerTransactionDTO;
import com.elleined.marketplaceapi.dto.atm.dto.WithdrawTransactionDTO;
import com.elleined.marketplaceapi.mapper.TransactionMapper;
import com.elleined.marketplaceapi.model.atm.transaction.DepositTransaction;
import com.elleined.marketplaceapi.model.atm.transaction.PeerToPeerTransaction;
import com.elleined.marketplaceapi.model.atm.transaction.Transaction;
import com.elleined.marketplaceapi.model.atm.transaction.WithdrawTransaction;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.service.atm.ATMService;
import com.elleined.marketplaceapi.service.atm.machine.transaction.TransactionService;
import com.elleined.marketplaceapi.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/users/{currentUserId}/atm")
@RequiredArgsConstructor
public class ATMController  {
    private final ATMService atmService;
    private final UserService userService;

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @PostMapping("/deposit")
    public DepositTransactionDTO requestDeposit(@PathVariable("currentUserId") int currentUserId,
                                         @RequestParam("amount") BigDecimal amount) {

        User currentUser = userService.getById(currentUserId);
        DepositTransaction depositTransaction = atmService.requestDeposit(currentUser, amount);
        return transactionMapper.toDepositTransactionDTO(depositTransaction);
    }


    @PostMapping("/withdraw")
    public WithdrawTransactionDTO requestWithdraw(@PathVariable("currentUserId") int currentUserId,
                                                  @RequestParam("amount") BigDecimal amount) {

        User currentUser = userService.getById(currentUserId);
        WithdrawTransaction withdrawTransaction = atmService.requestWithdraw(currentUser, amount);
        return transactionMapper.toWithdrawTransactionDTO(withdrawTransaction);
    }

    @PatchMapping("/withdraw/receive/{withdrawTransactionId}")
    public WithdrawTransactionDTO receiveWithdrawTransaction(@PathVariable("currentUserId") int currentUserId,
                                                             @PathVariable("withdrawTransactionId") int withdrawTransactionId) {
        User currentUser = userService.getById(currentUserId);
        WithdrawTransaction withdrawTransaction = transactionService.getWithdrawTransactionById(withdrawTransactionId);
        atmService.receiveWithdrawRequest(currentUser, withdrawTransaction);

        return transactionMapper.toWithdrawTransactionDTO(withdrawTransaction);
    }


    @PostMapping("/send-money/{receiverId}")
    public PeerToPeerTransactionDTO peerToPeer(@PathVariable("currentUserId") int senderId,
                                               @RequestParam("amount") BigDecimal sentAmount,
                                               @PathVariable("receiverId") int receiverId) {

        User sender = userService.getById(senderId);
        User receiver = userService.getById(receiverId);
        PeerToPeerTransaction peerToPeerTransaction = atmService.peerToPeer(sender, receiver, sentAmount);
        return transactionMapper.toPeer2PeerTransactionDTO(peerToPeerTransaction);
    }
}
