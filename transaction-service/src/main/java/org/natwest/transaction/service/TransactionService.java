package org.natwest.transaction.service;

import jakarta.transaction.Transactional;
import org.natwest.transaction.dto.request.MoneyRequest;
import org.natwest.transaction.dto.request.TransactionRequest;
import org.natwest.transaction.dto.response.TransactionResponse;
import org.natwest.transaction.entity.TransactionLedger;
import org.natwest.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponse deposit(String accountId, MoneyRequest request, String idempotencyKey) {
        validateAmount(request.amount());

        // TODO: Call account-service using WebClient
        // AccountResponse accountResponse =
        //        accountClient.deposit(
        //                accountId,
        //                request.amount(),
        //                idempotencyKey
        //        );

        BigDecimal balanceAfter = null;
        TransactionLedger transaction = createLedger(accountId, "DEPOSIT", request.amount(), balanceAfter, idempotencyKey);
        TransactionLedger saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    @Transactional
    public TransactionResponse withdraw(String accountId, MoneyRequest request, String idempotencyKey) {
        validateAmount(request.amount());

        // TODO: Call account-service using WebClient
        // AccountResponse accountResponse =
        //        accountClient.withdraw(
        //                accountId,
        //                request.amount(),
        //                idempotencyKey
        //        );

        BigDecimal balanceAfter = null;
        TransactionLedger transaction = createLedger(accountId, "WITHDRAW", request.amount(), balanceAfter, idempotencyKey);
        TransactionLedger saved = transactionRepository.save(transaction);

        return toResponse(saved);
    }

    @Transactional
    public TransactionResponse transfer(TransactionRequest request, String idempotencyKey) {
        validateAmount(request.amount());

        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new IllegalArgumentException("Source and destination accounts cannot be same");
        }

        /*
         * TODO:
         *
         * 1. Debit source account
         * 2. Credit destination account
         * 3. Create ledger entries
         *
         * These calls should be made through WebClient.
         */

        BigDecimal balanceAfter = null;
        TransactionLedger transaction = createLedger(request.fromAccountId(), "TRANSFER", request.amount(),
                balanceAfter, idempotencyKey);
        TransactionLedger saved = transactionRepository.save(transaction);

        return toResponse(saved);
    }

    private TransactionLedger createLedger(String accountId, String transactionType, BigDecimal amount, BigDecimal balanceAfter, String idempotencyKey) {

        return new TransactionLedger(UUID.randomUUID(), accountId, transactionType, amount, balanceAfter, idempotencyKey, Instant.now());
    }

    private TransactionResponse toResponse(TransactionLedger transaction) {

        return new TransactionResponse(
                transaction.getTransactionId(), transaction.getAccountId(),
                transaction.getTransactionType(),
                transaction.getAmount(), transaction.getBalanceAfter(),
                transaction.getReferenceId(), transaction.getCreatedAt());
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}