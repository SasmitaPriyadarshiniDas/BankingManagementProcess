package org.natwest.account.controller;

import jakarta.validation.Valid;
import org.natwest.account.dto.request.AccountRequest;
import org.natwest.account.dto.response.AccountResponse;
import org.natwest.account.dto.response.BalanceResponse;
import org.natwest.account.entity.Account;
import org.natwest.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
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

   /* @PostMapping("/{accountId}/deposit")
    public ResponseEntity<BalanceResponse> deposit(@PathVariable String accountId, @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                   @Valid @RequestBody MoneyRequest request) {
        BalanceResponse response = transactionService.deposit(accountId, request.amount(), idempotencyKey);
        return ResponseEntity.ok(response);
    }*/

    /*@PostMapping("/{accountId}/withdraw")
    public ResponseEntity<BalanceResponse> withdraw(
            @PathVariable String accountId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody MoneyRequest request) {

        BalanceResponse response = transactionService.withdraw(
                accountId,
                request.amount(),
                idempotencyKey
        );

    }*/
}
