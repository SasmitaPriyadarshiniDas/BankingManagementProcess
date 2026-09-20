package org.natwest.account.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.natwest.account.config.TransactionClient;
import org.natwest.account.dto.request.AccountRequest;
import org.natwest.account.dto.request.MoneyRequest;
import org.natwest.account.dto.response.BalanceResponse;
import org.natwest.account.entity.Account;
import org.natwest.account.service.AccountService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.math.BigDecimal;
import java.net.URI;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {

    private MockMvc mockMvc;
    @Mock
    private AccountService accountService;
    @Mock private TransactionClient transactionClient;
    @InjectMocks
    private AccountController accountController;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController) .build();
    }

    @Test
    public void shouldCreateAccountSuccessfully() throws Exception {
        Account account = new Account();
        account.setAccountId("ACC1001");
        account.setAccountHolder("John Doe");
        account.setBalance(new BigDecimal("1000.00"));
        account.setCurrency("GBP"); account.setStatus("ACTIVE");
        when(accountService.createAccount(any(AccountRequest.class))).thenReturn(account);
        String json = "{" + "\"accountId\":\"ACC1001\"," + "\"accountHolder\":\"John Doe\"," + "\"balance\":1000," + "\"currency\":\"GBP\"" + "}";
        mockMvc.perform( post(URI.create("/api/v1/accounts")).contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isCreated())
                .andExpect((ResultMatcher) jsonPath("$.accountId").value("ACC1001"))
                .andExpect((ResultMatcher) jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    public void shouldReturnBadRequestForInvalidAccount() throws Exception {
        String json = "{" + "\"accountId\":\"ACC1001\"," + "\"accountHolder\":\"\"," + "\"balance\":1000," + "\"currency\":\"GBP\"" + "}";
        mockMvc.perform( post("/api/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content(json)).andExpect(status().isBadRequest());
    }


   // @Test
    public void shouldDepositSuccessfully() throws Exception {
        BalanceResponse balanceResponse = new BalanceResponse( "ACC1001", new BigDecimal("1500.00"), "$" );
        when(transactionClient.deposit( eq("ACC1001"), any(MoneyRequest.class), eq("IDEMP-001") )).thenReturn(Mono.just(balanceResponse));
        String json = "{" + "\"amount\":500.00" + "}"; mockMvc.perform( post("/api/v1/accounts/ACC1001/deposit") .header("Idempotency-Key",
                        "IDEMP-001") .contentType(MediaType.APPLICATION_JSON) .content(json) ) .andDo(print())
            .andExpect(status().isOk()) .andExpect(jsonPath("$.accountId").value("ACC1001"))
                .andExpect(jsonPath("$.balance").value(1500.00))
                .andExpect(jsonPath("currency").value("$"));
    }

    @Test
    public void shouldReturnBadRequestForInvalidDepositAmount() throws Exception {

        String json = "{" + "\"amount\":-500.00" + "}";
        mockMvc.perform(post("/api/v1/accounts/ACC1001/deposit")
                                .header("Idempotency-Key", "IDEMP-002")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestForInvalidWithdrawAmount() throws Exception {

        String json = "{" + "\"amount\":-500.00" + "}";
        mockMvc.perform(post("/api/v1/accounts/ACC1001/withdraw")
                                .header("Idempotency-Key", "IDEMP-003")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenWithdrawIdempotencyKeyIsMissing() throws Exception {

        String json = "{" + "\"amount\":500.00" + "}";
        mockMvc.perform(post("/api/v1/accounts/ACC1001/withdraw")
                                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenWithdrawRequestBodyIsMissing() throws Exception {

        mockMvc.perform(post("/api/v1/accounts/ACC1001/withdraw")
                                .header("Idempotency-Key", "IDEMP-004")
                                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }



}
