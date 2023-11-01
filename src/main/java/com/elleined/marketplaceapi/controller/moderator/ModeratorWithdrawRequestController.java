package com.elleined.marketplaceapi.controller.moderator;

import com.elleined.marketplaceapi.dto.atm.dto.WithdrawTransactionDTO;
import com.elleined.marketplaceapi.mapper.TransactionMapper;
import com.elleined.marketplaceapi.model.Moderator;
import com.elleined.marketplaceapi.model.atm.transaction.WithdrawTransaction;
import com.elleined.marketplaceapi.service.atm.machine.transaction.WithdrawTransactionService;
import com.elleined.marketplaceapi.service.email.EmailService;
import com.elleined.marketplaceapi.service.moderator.ModeratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@RestController
@RequiredArgsConstructor
@RequestMapping("/moderators/{moderatorId}/withdraw-requests")
public class ModeratorWithdrawRequestController {

    private final ModeratorService moderatorService;

    private final TransactionMapper transactionMapper;
    private final WithdrawTransactionService withdrawTransactionService;

    private final EmailService emailService;

    @GetMapping
    List<WithdrawTransactionDTO> getAllPendingWithdrawRequest() {
        return moderatorService.getAllPendingWithdrawRequest().stream()
                .map(transactionMapper::toWithdrawTransactionDTO)
                .toList();
    }

    @PatchMapping("/{withdrawTransactionId}/release")
    void releaseWithdrawRequest(@PathVariable("moderatorId") int moderatorId,
                                @PathVariable("withdrawTransactionId") Integer withdrawTransactionId,
                                @RequestPart("proofOfTransaction") MultipartFile proofOfTransaction) throws IOException {

        Moderator moderator = moderatorService.getById(moderatorId);
        WithdrawTransaction withdrawTransaction = withdrawTransactionService.getById(withdrawTransactionId);
        moderatorService.release(moderator, withdrawTransaction, proofOfTransaction);
        emailService.sendReleaseWithdrawMail(withdrawTransaction);
    }

    @PatchMapping("/release")
    void releaseAllWithdrawRequest(@PathVariable("moderatorId") int moderatorId,
                                   @RequestBody Set<Integer> withdrawTransactionIds) {

        Moderator moderator = moderatorService.getById(moderatorId);
        Set<WithdrawTransaction> withdrawTransactions = new HashSet<>(withdrawTransactionService.getAll(withdrawTransactionIds));
        moderatorService.releaseAllWithdrawRequest(moderator, withdrawTransactions);
    }

    @PatchMapping("/{withdrawTransactionId}/reject")
    void rejectWithdrawRequest(@PathVariable("moderatorId") int moderatorId,
                               @PathVariable("withdrawTransactionId") Integer withdrawTransactionId) {

        Moderator moderator = moderatorService.getById(moderatorId);
        WithdrawTransaction withdrawTransaction = withdrawTransactionService.getById(withdrawTransactionId);
        moderatorService.reject(moderator, withdrawTransaction);
        emailService.sendRejectedWithdrawMail(withdrawTransaction);
    }

    @PatchMapping("/reject")
    void rejectAllWithdrawRequest(@PathVariable("moderatorId") int moderatorId,
                                  @RequestBody Set<Integer> withdrawTransactionIds) {

        Moderator moderator = moderatorService.getById(moderatorId);
        Set<WithdrawTransaction> withdrawTransactions = new HashSet<>(withdrawTransactionService.getAll(withdrawTransactionIds));
        moderatorService.rejectAllWithdrawRequest(moderator, withdrawTransactions);
    }
}
