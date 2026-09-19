package org.natwest.transaction.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "transaction_ledger",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_reference_id", columnNames = "reference_id")
        }
)
public class TransactionLedger {

    @Id
    @GeneratedValue
    private UUID transactionId;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false)
    private String transactionType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(nullable = false, unique = true)
    private String referenceId;

    @Column(nullable = false)
    private Instant createdAt;

    public TransactionLedger() {
    }

    public TransactionLedger(UUID transactionId, String accountId, String transactionType, BigDecimal amount,
            BigDecimal balanceAfter, String referenceId, Instant createdAt) {

        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.referenceId = referenceId;
        this.createdAt = createdAt;
    }

}