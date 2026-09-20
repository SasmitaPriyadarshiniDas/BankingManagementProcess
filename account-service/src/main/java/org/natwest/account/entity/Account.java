package org.natwest.account.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "account")
@Getter
@Setter
@AllArgsConstructor
public class Account {

    @Id
    @Column(name = "account_id", length = 50)
    private String accountId;

    @Column(name = "account_holder", nullable = false)
    private String accountHolder;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 20)
    private String status;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Account() {
    }

    public Account(String accountId, String accountHolder, BigDecimal balance, String currency) {

        this.accountId = accountId;
        this.accountHolder = accountHolder;
        this.balance = balance;
        this.currency = currency;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void deposit(BigDecimal amount) {
        balance = balance.add(amount);
        updatedAt = Instant.now();
    }

    public void withdraw(BigDecimal amount) {

        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        balance = balance.subtract(amount);
        updatedAt = Instant.now();
    }


}
