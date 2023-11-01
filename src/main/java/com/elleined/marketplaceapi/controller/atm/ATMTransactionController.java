package com.elleined.marketplaceapi.controller.atm;

import com.elleined.marketplaceapi.dto.atm.dto.DepositTransactionDTO;
import com.elleined.marketplaceapi.dto.atm.dto.PeerToPeerTransactionDTO;
import com.elleined.marketplaceapi.dto.atm.dto.WithdrawTransactionDTO;
import com.elleined.marketplaceapi.mapper.TransactionMapper;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.service.atm.machine.transaction.DepositTransactionService;
import com.elleined.marketplaceapi.service.atm.machine.transaction.PeerToPeerTransactionService;
import com.elleined.marketplaceapi.service.atm.machine.transaction.WithdrawTransactionService;
import com.elleined.marketplaceapi.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/{currentUserId}/atm-transactions")
@RequiredArgsConstructor
public class ATMTransactionController {
    private final UserService userService;

    private final WithdrawTransactionService withdrawTransactionService;
    private final DepositTransactionService depositTransactionService;
    private final PeerToPeerTransactionService peerToPeerTransactionService;
    private final TransactionMapper transactionMapper;

    @GetMapping("/withdraw")
    public List<WithdrawTransactionDTO> getAllWithdrawalTransactions(@PathVariable("currentUserId") int currentUserId) {
        User currentUser = userService.getById(currentUserId);
        return withdrawTransactionService.getAll(currentUser).stream()
                .map(transactionMapper::toWithdrawTransactionDTO)
                .toList();
    }

    @GetMapping("/deposit")
    public List<DepositTransactionDTO> getAllDepositTransactions(@PathVariable("currentUserId") int currentUserId) {
        User currentUser = userService.getById(currentUserId);
        return depositTransactionService.getAll(currentUser).stream()
                .map(transactionMapper::toDepositTransactionDTO)
                .toList();
    }

    @GetMapping("/receive-money")
    public List<PeerToPeerTransactionDTO> getAllReceiveMoneyTransactions(@PathVariable("currentUserId") int currentUserId) {
        User currentUser = userService.getById(currentUserId);
        return peerToPeerTransactionService.getAllReceivedTransaction(currentUser).stream()
                .map(transactionMapper::toPeer2PeerTransactionDTO)
                .toList();
    }

    @GetMapping("/sent-money")
    public List<PeerToPeerTransactionDTO> getAllSentMoneyTransactions(@PathVariable("currentUserId") int currentUserId) {
        User currentUser = userService.getById(currentUserId);
        return peerToPeerTransactionService.getAllSentTransactions(currentUser).stream()
                .map(transactionMapper::toPeer2PeerTransactionDTO)
                .toList();
    }
}
