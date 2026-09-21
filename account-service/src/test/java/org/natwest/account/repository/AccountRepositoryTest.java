package org.natwest.account.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.natwest.account.AccountServiceApplication;
import org.natwest.account.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AccountServiceApplication.class)
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    // @Test
    public void shouldFindAccountForUpdate() {
        Account account = new Account("ACC1001", "John Doe", new BigDecimal("1000.00"), "GBP");
        accountRepository.save(account);
        Optional<Account> result = accountRepository.findByIdForUpdate("ACC1001");
        assertTrue(result.isPresent());
        Account foundAccount = result.get();
        assertEquals("ACC1001", foundAccount.getAccountId());
        assertEquals("John Doe", foundAccount.getAccountHolder());
        assertEquals(new BigDecimal("1000.00"), foundAccount.getBalance());
        assertEquals("GBP", foundAccount.getCurrency());
    }

    @Test
    public void shouldReturnEmptyWhenAccountDoesNotExist() {
        Optional<Account> result = accountRepository.findByIdForUpdate("ACC9999");
        assertFalse(result.isPresent());
    }
}
