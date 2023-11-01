package com.elleined.marketplaceapi.controller.moderator;

import com.elleined.marketplaceapi.dto.atm.dto.DepositTransactionDTO;
import com.elleined.marketplaceapi.mapper.TransactionMapper;
import com.elleined.marketplaceapi.model.Moderator;
import com.elleined.marketplaceapi.model.atm.transaction.DepositTransaction;
import com.elleined.marketplaceapi.service.atm.machine.transaction.DepositTransactionService;
import com.elleined.marketplaceapi.service.email.EmailService;
import com.elleined.marketplaceapi.service.moderator.ModeratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/moderators/{moderatorId}/deposit-requests")
public class ModeratorDepositRequestController {
    private final ModeratorService moderatorService;

    private final DepositTransactionService depositTransactionService;
    private final TransactionMapper transactionMapper;

    private final EmailService emailService;

    @GetMapping
    List<DepositTransactionDTO> getAllPendingDepositRequest() {
        return moderatorService.getAllPendingDepositRequest().stream()
                .map(transactionMapper::toDepositTransactionDTO)
                .toList();
    }

    @PatchMapping("/{depositTransactionId}/release")
    void release(@PathVariable("moderatorId") int moderatorId,
                 @PathVariable("depositTransactionId") int depositTransactionId) {

        Moderator moderator = moderatorService.getById(moderatorId);
        DepositTransaction depositTransaction = depositTransactionService.getById(depositTransactionId);
        moderatorService.release(moderator, depositTransaction);
        emailService.sendReleaseDepositMail(depositTransaction);
    }
    @PatchMapping("/release")
    void releaseAllDepositRequest(@PathVariable("moderatorId") int moderatorId,
                                  @RequestBody Set<Integer> depositTransactionsId) {

        Moderator moderator = moderatorService.getById(moderatorId);
        Set<DepositTransaction> depositTransactions = new HashSet<>(depositTransactionService.getAll(depositTransactionsId));
        moderatorService.releaseAllDepositRequest(moderator, depositTransactions);
    }
    @PatchMapping("/{depositTransactionId}/reject")
    void reject(@PathVariable("moderatorId") int moderatorId,
                @PathVariable("depositTransactionId") int depositTransactionId) {

        Moderator moderator = moderatorService.getById(moderatorId);
        DepositTransaction depositTransaction = depositTransactionService.getById(depositTransactionId);
        moderatorService.reject(moderator, depositTransaction);
        emailService.sendRejectDepositMail(depositTransaction);
    }

    @PatchMapping("/reject")
    void rejectAllDepositRequest(@PathVariable("moderatorId") int moderatorId,
                                 @RequestBody Set<Integer> depositTransactionsId) {

        Moderator moderator = moderatorService.getById(moderatorId);
        Set<DepositTransaction> depositTransactions = new HashSet<>(depositTransactionService.getAll(depositTransactionsId));
        moderatorService.rejectAllDepositRequest(moderator, depositTransactions);
    }
}
