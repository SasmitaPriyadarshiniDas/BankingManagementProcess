package org.natwest.account.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.natwest.account.dto.request.AccountRequest;
import org.natwest.account.dto.response.BalanceResponse;
import org.natwest.account.entity.Account;
import org.natwest.account.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Before
    public void setUp() {
    }

    @Test
    public void shouldCreateAccountSuccessfully() {
        AccountRequest request = new AccountRequest("John Doe", "GBP");
        Account savedAccount = new Account("ACC12345678", "John Doe", BigDecimal.ZERO, "GBP");

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        Account result = accountService.createAccount(request);
        assertNotNull(result);
        assertEquals("ACC12345678", result.getAccountId());
        assertEquals("John Doe", result.getAccountHolder());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals("GBP", result.getCurrency());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void shouldCreateAccountWithZeroBalance() {

        AccountRequest request = new AccountRequest("Alice Smith", "USD");
        Account savedAccount = new Account("ACCABC12345", "Alice Smith", BigDecimal.ZERO, "USD");
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        Account result = accountService.createAccount(request);
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void shouldGenerateAccountIdWithCorrectFormat() {

        AccountRequest request = new AccountRequest("John Doe", "GBP");
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Account result = accountService.createAccount(request);
        verify(accountRepository).save(accountCaptor.capture());
        Account savedAccount = accountCaptor.getValue();
        assertNotNull(savedAccount.getAccountId());
        assertTrue(savedAccount.getAccountId().startsWith("ACC"));

        // ACC + 8 characters
        assertEquals(11, savedAccount.getAccountId().length());
        assertEquals(result.getAccountId(), savedAccount.getAccountId());
    }

    @Test
    public void shouldSaveCorrectAccountDetails() {

        AccountRequest request = new AccountRequest("Jane Doe", "EUR");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation ->
                        invocation.getArgument(0));
        accountService.createAccount(request);
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        verify(accountRepository).save(captor.capture());
        Account account = captor.getValue();

        assertEquals("Jane Doe", account.getAccountHolder());
        assertEquals("EUR", account.getCurrency());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertNotNull(account.getAccountId());
    }

    @Test
    public void shouldPropagateRepositoryException() {
        AccountRequest request = new AccountRequest("John Doe", "GBP");
        when(accountRepository.save(any(Account.class))).thenThrow(new RuntimeException("Database error"));

        try {
            accountService.createAccount(request);
            fail("Expected RuntimeException");
        } catch (RuntimeException ex) {
            assertEquals("Database error", ex.getMessage());
        }
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void shouldGetBalanceSuccessfully() {

        Account account = new Account("ACC1001", "John Doe", new BigDecimal("1500.00"), "GBP");
        when(accountRepository.findById("ACC1001")).thenReturn(Optional.of(account));
        BalanceResponse result = accountService.getBalance("ACC1001");
        assertNotNull(result);
        assertEquals("ACC1001", result.accountId());
        assertEquals(new BigDecimal("1500.00"), result.balance());
        assertEquals("GBP", result.currency());
        verify(accountRepository, times(1)).findById("ACC1001");
    }

    @Test
    public void shouldThrowExceptionWhenAccountNotFound() {

        when(accountRepository.findById("ACC9999")).thenReturn(Optional.empty());

        try {
            accountService.getBalance("ACC9999");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("Account not found: ACC9999", ex.getMessage());
        }
        verify(accountRepository, times(1)).findById("ACC9999");
    }

    @Test
    public void shouldFindAccountOnlyOnce() {

        Account account = new Account("ACC1001", "John Doe", new BigDecimal("1000.00"), "GBP");
        when(accountRepository.findById("ACC1001")).thenReturn(Optional.of(account));
        accountService.getBalance("ACC1001");
        verify(accountRepository, times(1)).findById("ACC1001");
    }


}

