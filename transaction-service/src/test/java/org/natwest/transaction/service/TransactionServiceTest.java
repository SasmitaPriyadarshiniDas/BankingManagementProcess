package org.natwest.transaction.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.natwest.transaction.config.AccountClient;
import org.natwest.transaction.dto.request.MoneyRequest;
import org.natwest.transaction.dto.request.TransactionRequest;
import org.natwest.transaction.dto.response.TransactionResponse;
import org.natwest.transaction.entity.TransactionLedger;
import org.natwest.transaction.repository.TransactionRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {

    private static final String ACCOUNT_ID = "ACC1001";
    private static final String TO_ACCOUNT_ID = "ACC1002";
    private static final String IDEMPOTENCY_KEY = "IDEMP-001";
    private static final BigDecimal AMOUNT = new BigDecimal("500.00");
    private static final BigDecimal BALANCE_AFTER = new BigDecimal("1500.00");

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private TransactionService transactionService;

    private MoneyRequest moneyRequest;
    private TransactionLedger savedLedger;

    @Before
    public void setUp() {
        moneyRequest = new MoneyRequest(AMOUNT);
        savedLedger = new TransactionLedger(UUID.randomUUID(), ACCOUNT_ID, "DEPOSIT",
                AMOUNT, BALANCE_AFTER, IDEMPOTENCY_KEY, Instant.now());
    }

    // ----------------------------------------------------------------- deposit

    @Test
    public void shouldDepositSuccessfully() {
        TransactionResponse accountResponse = new TransactionResponse(
                UUID.randomUUID(), ACCOUNT_ID, "DEPOSIT", AMOUNT,
                BALANCE_AFTER, IDEMPOTENCY_KEY, Instant.now());

        when(accountClient.deposit(eq(ACCOUNT_ID), eq(AMOUNT), eq(IDEMPOTENCY_KEY)))
                .thenReturn(Mono.just(accountResponse));
        when(transactionRepository.save(any(TransactionLedger.class))).thenReturn(savedLedger);

        TransactionResponse result = transactionService.deposit(ACCOUNT_ID, moneyRequest, IDEMPOTENCY_KEY).block();

        assertNotNull(result);
        assertEquals(ACCOUNT_ID, result.accountId());
        assertEquals("DEPOSIT", result.transactionType());
        assertEquals(0, AMOUNT.compareTo(result.amount()));
        assertEquals(0, BALANCE_AFTER.compareTo(result.balanceAfter()));
        assertEquals(IDEMPOTENCY_KEY, result.referenceId());

        verify(accountClient, times(1)).deposit(eq(ACCOUNT_ID), eq(AMOUNT), eq(IDEMPOTENCY_KEY));
        verify(transactionRepository, times(1)).save(any(TransactionLedger.class));
    }

    @Test
    public void shouldThrowWhenDepositAmountIsZero() {
        MoneyRequest invalidRequest = new MoneyRequest(BigDecimal.ZERO);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.deposit(ACCOUNT_ID, invalidRequest, IDEMPOTENCY_KEY));

        assertEquals("Amount must be greater than zero", exception.getMessage());
        verifyNoInteractions(accountClient);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    public void shouldThrowWhenDepositAmountIsNegative() {
        MoneyRequest invalidRequest = new MoneyRequest(new BigDecimal("-500.00"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.deposit(ACCOUNT_ID, invalidRequest, IDEMPOTENCY_KEY));

        assertEquals("Amount must be greater than zero", exception.getMessage());
        verifyNoInteractions(accountClient);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    public void shouldThrowWhenDepositAmountIsNull() {
        MoneyRequest invalidRequest = new MoneyRequest(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.deposit(ACCOUNT_ID, invalidRequest, IDEMPOTENCY_KEY));

        assertEquals("Amount must be greater than zero", exception.getMessage());
        verifyNoInteractions(accountClient);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    public void shouldPropagateErrorWhenAccountClientDepositFails() {
        when(accountClient.deposit(eq(ACCOUNT_ID), eq(AMOUNT), eq(IDEMPOTENCY_KEY)))
                .thenReturn(Mono.error(new RuntimeException("account service unavailable")));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.deposit(ACCOUNT_ID, moneyRequest, IDEMPOTENCY_KEY).block());

        assertEquals("account service unavailable", exception.getMessage());
        verifyNoInteractions(transactionRepository);
    }

    // ----------------------------------------------------------------- withdraw

    @Test
    public void shouldWithdrawSuccessfully() {
        TransactionResponse accountResponse = new TransactionResponse(
                UUID.randomUUID(), ACCOUNT_ID, "WITHDRAW", AMOUNT,
                BALANCE_AFTER, IDEMPOTENCY_KEY, Instant.now());

        TransactionLedger withdrawLedger = new TransactionLedger(UUID.randomUUID(), ACCOUNT_ID, "WITHDRAW",
                AMOUNT, BALANCE_AFTER, IDEMPOTENCY_KEY, Instant.now());

        when(accountClient.withdraw(eq(ACCOUNT_ID), eq(AMOUNT), eq(IDEMPOTENCY_KEY)))
                .thenReturn(Mono.just(accountResponse));
        when(transactionRepository.save(any(TransactionLedger.class))).thenReturn(withdrawLedger);

        TransactionResponse result = transactionService.withdraw(ACCOUNT_ID, moneyRequest, IDEMPOTENCY_KEY).block();

        assertNotNull(result);
        assertEquals(ACCOUNT_ID, result.accountId());
        assertEquals("WITHDRAW", result.transactionType());
        assertEquals(0, AMOUNT.compareTo(result.amount()));

        verify(accountClient, times(1)).withdraw(eq(ACCOUNT_ID), eq(AMOUNT), eq(IDEMPOTENCY_KEY));
        verify(transactionRepository, times(1)).save(any(TransactionLedger.class));
    }

    @Test
    public void shouldThrowWhenWithdrawAmountIsZero() {
        MoneyRequest invalidRequest = new MoneyRequest(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.withdraw(ACCOUNT_ID, invalidRequest, IDEMPOTENCY_KEY));

        verifyNoInteractions(accountClient);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    public void shouldPropagateErrorWhenAccountClientWithdrawFails() {
        when(accountClient.withdraw(eq(ACCOUNT_ID), eq(AMOUNT), eq(IDEMPOTENCY_KEY)))
                .thenReturn(Mono.error(new RuntimeException("insufficient funds")));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.withdraw(ACCOUNT_ID, moneyRequest, IDEMPOTENCY_KEY).block());

        assertEquals("insufficient funds", exception.getMessage());
        verifyNoInteractions(transactionRepository);
    }

    // ----------------------------------------------------------------- transfer

    @Test
    public void shouldTransferSuccessfully() {
        TransactionRequest request = new TransactionRequest(ACCOUNT_ID, TO_ACCOUNT_ID, AMOUNT);

        TransactionResponse sourceResponse = new TransactionResponse(
                UUID.randomUUID(), ACCOUNT_ID, "WITHDRAW", AMOUNT,
                new BigDecimal("900.00"), IDEMPOTENCY_KEY, Instant.now());

        TransactionResponse destinationResponse = new TransactionResponse(
                UUID.randomUUID(), TO_ACCOUNT_ID, "DEPOSIT", AMOUNT,
                new BigDecimal("600.00"), IDEMPOTENCY_KEY, Instant.now());

        TransactionLedger transferLedger = new TransactionLedger(UUID.randomUUID(), ACCOUNT_ID, "TRANSFER",
                AMOUNT, new BigDecimal("900.00"), IDEMPOTENCY_KEY, Instant.now());

        when(accountClient.withdraw(eq(ACCOUNT_ID), eq(AMOUNT), eq(IDEMPOTENCY_KEY)))
                .thenReturn(Mono.just(sourceResponse));
        when(accountClient.deposit(eq(TO_ACCOUNT_ID), eq(AMOUNT), eq(IDEMPOTENCY_KEY)))
                .thenReturn(Mono.just(destinationResponse));
        when(transactionRepository.save(any(TransactionLedger.class))).thenReturn(transferLedger);

        TransactionResponse result = transactionService.transfer(request, IDEMPOTENCY_KEY).block();

        assertNotNull(result);
        assertEquals(ACCOUNT_ID, result.accountId());
        assertEquals("TRANSFER", result.transactionType());
        assertEquals(0, AMOUNT.compareTo(result.amount()));
        assertEquals(0, new BigDecimal("900.00").compareTo(result.balanceAfter()));

        verify(accountClient, times(1)).withdraw(eq(ACCOUNT_ID), eq(AMOUNT), eq(IDEMPOTENCY_KEY));
        verify(accountClient, times(1)).deposit(eq(TO_ACCOUNT_ID), eq(AMOUNT), eq(IDEMPOTENCY_KEY));
        verify(transactionRepository, times(1)).save(any(TransactionLedger.class));
    }

    @Test
    public void shouldThrowWhenTransferAmountIsZero() {
        TransactionRequest invalidRequest = new TransactionRequest(ACCOUNT_ID, TO_ACCOUNT_ID, BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.transfer(invalidRequest, IDEMPOTENCY_KEY));

        verifyNoInteractions(accountClient);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    public void shouldReturnErrorWhenSourceAndDestinationAccountsAreSame() {
        TransactionRequest invalidRequest = new TransactionRequest(ACCOUNT_ID, ACCOUNT_ID, AMOUNT);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transfer(invalidRequest, IDEMPOTENCY_KEY).block());

        assertEquals("Source and destination accounts cannot be same", exception.getMessage());
        verifyNoInteractions(accountClient);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    public void shouldPropagateErrorWhenWithdrawFailsDuringTransfer() {
        TransactionRequest request = new TransactionRequest(ACCOUNT_ID, TO_ACCOUNT_ID, AMOUNT);

        when(accountClient.withdraw(eq(ACCOUNT_ID), eq(AMOUNT), eq(IDEMPOTENCY_KEY)))
                .thenReturn(Mono.error(new RuntimeException("insufficient funds")));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.transfer(request, IDEMPOTENCY_KEY).block());

        assertEquals("insufficient funds", exception.getMessage());
        verify(accountClient, times(1)).withdraw(eq(ACCOUNT_ID), eq(AMOUNT), eq(IDEMPOTENCY_KEY));
        verify(accountClient, org.mockito.Mockito.never())
                .deposit(any(String.class), any(BigDecimal.class), any(String.class));
        verifyNoInteractions(transactionRepository);
    }

    @Test
    public void shouldPropagateErrorWhenDepositFailsDuringTransfer() {
        TransactionRequest request = new TransactionRequest(ACCOUNT_ID, TO_ACCOUNT_ID, AMOUNT);
        TransactionResponse sourceResponse =
                new TransactionResponse(
                        UUID.randomUUID(),                 // transactionId
                        ACCOUNT_ID,                         // accountId
                        "TRANSFER",                          // transactionType
                        AMOUNT,                              // amount
                        new BigDecimal("900.00"),            // balanceAfter
                        IDEMPOTENCY_KEY,                     // referenceId
                        Instant.now()                        // createdAt
                );

        when(accountClient.withdraw(eq(ACCOUNT_ID), eq(AMOUNT), eq(IDEMPOTENCY_KEY)))
                .thenReturn(Mono.just(sourceResponse));
        when(accountClient.deposit(eq(TO_ACCOUNT_ID), eq(AMOUNT), eq(IDEMPOTENCY_KEY)))
                .thenReturn(Mono.error(new RuntimeException("destination account not found")));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.transfer(request, IDEMPOTENCY_KEY).block());

        assertEquals("destination account not found", exception.getMessage());
        verifyNoInteractions(transactionRepository);
    }
}