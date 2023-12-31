package com.elleined.marketplaceapi.dto.atm.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class DepositTransactionDTO extends TransactionDTO {

    private int userId;

    @Builder(builderMethodName = "depositTransactionBuilder")
    public DepositTransactionDTO(int id, String trn, BigDecimal amount, String status, LocalDateTime transactionDate, String proofOfTransaction, float transactionFee, int userId) {
        super(id, trn, amount, status, transactionDate, proofOfTransaction, transactionFee);
        this.userId = userId;
    }
}
