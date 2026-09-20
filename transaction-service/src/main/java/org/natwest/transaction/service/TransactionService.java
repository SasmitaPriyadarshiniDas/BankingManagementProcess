package org.natwest.transaction.service;

import jakarta.transaction.Transactional;
import org.natwest.transaction.config.AccountClient;
import org.natwest.transaction.dto.request.MoneyRequest;
import org.natwest.transaction.dto.request.TransactionRequest;
import org.natwest.transaction.dto.response.TransactionResponse;
import org.natwest.transaction.entity.TransactionLedger;
import org.natwest.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountClient accountClient) {

        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
    }

    /**
     * Deposit money into an account.
     */
    @Transactional
    public Mono<TransactionResponse> deposit(String accountId, MoneyRequest request, String idempotencyKey) {
        validateAmount(request.amount());

        return accountClient.deposit(accountId, request.amount(), idempotencyKey)
                .map(accountResponse -> {
                    TransactionLedger transaction = createLedger(accountId, "DEPOSIT", request.amount(),
                                    accountResponse.balanceAfter(), idempotencyKey);

                    TransactionLedger saved = transactionRepository.save(transaction);
                    return toResponse(saved);
                });
    }

    /**
     * Withdraw money from an account.
     */
    @Transactional
    public Mono<TransactionResponse> withdraw(String accountId, MoneyRequest request, String idempotencyKey) {
        validateAmount(request.amount());

        return accountClient.withdraw(accountId, request.amount(), idempotencyKey)
                .map(accountResponse -> {
                    TransactionLedger transaction = createLedger(accountId, "WITHDRAW", request.amount(),
                                    accountResponse.balanceAfter(), idempotencyKey);

                    TransactionLedger saved = transactionRepository.save(transaction);
                    return toResponse(saved);
                });
    }

    /**
     * Transfer money between two accounts.
     */
    @Transactional
    public Mono<TransactionResponse> transfer(TransactionRequest request, String idempotencyKey) {
        validateAmount(request.amount());
        if (request.fromAccountId().equals(request.toAccountId())) {
            return Mono.error(new IllegalArgumentException("Source and destination accounts cannot be same"));
        }

        /*
         * 1. Debit source account
         * 2. Credit destination account
         * 3. Save transaction ledger
         */

        return accountClient.withdraw(request.fromAccountId(), request.amount(), idempotencyKey)
                .flatMap(sourceAccount ->
                        accountClient.deposit(request.toAccountId(), request.amount(), idempotencyKey)
                                .map(destinationAccount -> {

                                    TransactionLedger transaction = createLedger(
                                                    request.fromAccountId(), "TRANSFER",
                                                    request.amount(), sourceAccount.balanceAfter(), idempotencyKey);

                                    TransactionLedger saved = transactionRepository.save(transaction);
                                    return toResponse(saved);
                                })
                );
    }

    /**
     * Creates ledger entry.
     */
    private TransactionLedger createLedger(String accountId, String transactionType,
                                           BigDecimal amount, BigDecimal balanceAfter, String idempotencyKey) {
        return new TransactionLedger(UUID.randomUUID(), accountId, transactionType,
                amount, balanceAfter, idempotencyKey, Instant.now());
    }

    /**
     * Converts entity to response.
     */
    private TransactionResponse toResponse(
            TransactionLedger transaction) {

        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getAccountId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getReferenceId(),
                transaction.getCreatedAt()
        );
    }

    /**
     * Validates transaction amount.
     */
    private void validateAmount(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}