package org.natwest.account.controller;

import jakarta.validation.Valid;
import org.natwest.account.config.TransactionClient;
import org.natwest.account.dto.request.AccountRequest;
import org.natwest.account.dto.request.MoneyRequest;
import org.natwest.account.dto.response.AccountResponse;
import org.natwest.account.dto.response.BalanceResponse;
import org.natwest.account.entity.Account;
import org.natwest.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final TransactionClient transactionClient;

    public AccountController(AccountService accountService, TransactionClient transactionClient) {
        this.accountService = accountService;
        this.transactionClient = transactionClient;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request) {

        Account account = accountService.createAccount(request);
        AccountResponse response = new AccountResponse(account.getAccountId(), account.getAccountHolder(),
                account.getBalance(), account.getCurrency(), account.getStatus()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String accountId) {
        BalanceResponse response = accountService.getBalance(accountId);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{accountId}/deposit")
    public Mono<ResponseEntity<BalanceResponse>> deposit(@PathVariable String accountId,
                                                         @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                         @Valid @RequestBody MoneyRequest request) {

        return transactionClient
                .deposit(accountId, request, idempotencyKey)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{accountId}/withdraw")
    public Mono<ResponseEntity<BalanceResponse>> withdraw(@PathVariable String accountId, @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody MoneyRequest request) {

        return transactionClient.withdraw(accountId, request, idempotencyKey).map(ResponseEntity::ok);
    }
}
