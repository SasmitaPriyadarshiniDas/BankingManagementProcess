package org.natwest.transaction.controller;

import jakarta.servlet.ServletException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.natwest.transaction.dto.request.TransactionRequest;
import org.natwest.transaction.service.TransactionService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class TransactionControllerTest {

    private static final String TRANSFER_URL = "/api/v1/transfers";
    private static final String DEPOSIT_URL = "/api/v1/transfers/ACC1001/deposit";

    // ASSUMED field names - change to match your TransactionRequest DTO
    private static final String VALID_TRANSFER_JSON = "{"
            + "\"fromAccountId\":\"ACC1001\","
            + "\"toAccountId\":\"ACC1002\","
            + "\"amount\":100.50,"
            + "\"currency\":\"GBP\""
            + "}";

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
    }

    // ---------------------------------------------------------------- transfer

    @Test
    public void shouldTransferSuccessfully() throws Exception {
        mockMvc.perform(post(TRANSFER_URL)
                        .header("Idempotency-Key", "IDEMP-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TRANSFER_JSON))
                .andExpect(status().isNoContent());

        verify(transactionService).transfer(any(TransactionRequest.class), eq("IDEMP-001"));
    }

    @Test
    public void shouldReturnBadRequestWhenTransferIdempotencyKeyIsMissing() throws Exception {
        mockMvc.perform(post(TRANSFER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TRANSFER_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    public void shouldReturnBadRequestForInvalidTransferRequest() throws Exception {
        mockMvc.perform(post(TRANSFER_URL)
                        .header("Idempotency-Key", "IDEMP-002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    public void shouldReturnBadRequestWhenTransferRequestBodyIsMissing() throws Exception {
        mockMvc.perform(post(TRANSFER_URL)
                        .header("Idempotency-Key", "IDEMP-003")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    public void shouldReturnBadRequestForMalformedTransferJson() throws Exception {
        mockMvc.perform(post(TRANSFER_URL)
                        .header("Idempotency-Key", "IDEMP-004")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    public void shouldReturnUnsupportedMediaTypeForTransfer() throws Exception {
        mockMvc.perform(post(TRANSFER_URL)
                        .header("Idempotency-Key", "IDEMP-005")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hello"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void shouldReturnMethodNotAllowedForGetOnTransfer() throws Exception {
        mockMvc.perform(get(TRANSFER_URL))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void shouldPropagateExceptionWhenTransferServiceFails() {
        doThrow(new IllegalStateException("insufficient funds"))
                .when(transactionService).transfer(any(TransactionRequest.class), eq("IDEMP-006"));

        // standalone MockMvc (no @ControllerAdvice) rethrows unhandled exceptions
        assertThrows(ServletException.class, () ->
                mockMvc.perform(post(TRANSFER_URL)
                        .header("Idempotency-Key", "IDEMP-006")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TRANSFER_JSON)));
    }

    // ----------------------------------------------------------------- deposit



    @Test
    public void shouldReturnBadRequestWhenDepositIdempotencyKeyIsMissing() throws Exception {
        mockMvc.perform(post(DEPOSIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":500.00}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    public void shouldReturnBadRequestForInvalidDepositAmount() throws Exception {
        mockMvc.perform(post(DEPOSIT_URL)
                        .header("Idempotency-Key", "IDEMP-102")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":-500.00}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    public void shouldReturnBadRequestWhenDepositRequestBodyIsMissing() throws Exception {
        mockMvc.perform(post(DEPOSIT_URL)
                        .header("Idempotency-Key", "IDEMP-103")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    public void shouldReturnBadRequestForMalformedDepositJson() throws Exception {
        mockMvc.perform(post(DEPOSIT_URL)
                        .header("Idempotency-Key", "IDEMP-104")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bad"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    public void shouldPropagateErrorWhenDepositServiceFails() throws Exception {
        when(transactionService.deposit(any(), any(), any()))
                .thenReturn(Mono.error(new IllegalStateException("boom")));

        MvcResult mvcResult = mockMvc.perform(post(DEPOSIT_URL)
                        .header("Idempotency-Key", "IDEMP-105")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":500.00}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        assertThrows(Exception.class, () -> mockMvc.perform(asyncDispatch(mvcResult)));
    }


}
