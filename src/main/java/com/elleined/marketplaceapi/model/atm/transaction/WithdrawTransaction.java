package com.elleined.marketplaceapi.model.atm.transaction;

import com.elleined.marketplaceapi.model.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_transaction_withdraw")
@NoArgsConstructor
@Getter
@Setter
public class WithdrawTransaction extends Transaction {

    @ManyToOne(optional = false)
    @JoinColumn(
            nullable = false,
            updatable = false,
            name = "user_id",
            referencedColumnName = "user_id"
    )
    private User user;

    @Builder
    public WithdrawTransaction(int id, String trn, BigDecimal amount, LocalDateTime transactionDate, Status status, User user) {
        super(id, trn, amount, transactionDate, status);
        this.user = user;
    }

    public boolean isRelease() {
        return this.getStatus() == Status.RELEASE;
    }
}
