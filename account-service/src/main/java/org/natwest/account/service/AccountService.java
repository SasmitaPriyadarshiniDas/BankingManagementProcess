package org.natwest.account.service;

import jakarta.transaction.Transactional;
import org.natwest.account.dto.request.AccountRequest;
import org.natwest.account.dto.response.BalanceResponse;
import org.natwest.account.entity.Account;
import org.natwest.account.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(AccountRequest request) {
        String accountId = "ACC" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Account account = new Account(accountId, request.accountHolder(), BigDecimal.ZERO, request.currency());
        return accountRepository.save(account);
    }

    @Transactional//(readOnly = true)
    public BalanceResponse getBalance(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
        return new BalanceResponse(account.getAccountId(), account.getBalance(), account.getCurrency());
    }
}
