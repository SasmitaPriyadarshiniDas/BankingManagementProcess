package org.natwest.transaction.config;

import org.natwest.transaction.dto.request.MoneyRequest;
import org.natwest.transaction.dto.response.TransactionResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
public class AccountClient {

    private final WebClient webClient;

    public AccountClient(WebClient accountWebClient) {
        this.webClient = accountWebClient;
    }

    public Mono<TransactionResponse> deposit(String accountId, BigDecimal amount, String idempotencyKey) {

        return webClient.post().uri("/accounts/{accountId}/deposit", accountId)
                .header("Idempotency-Key", idempotencyKey)
                .bodyValue(new MoneyRequest(amount))
                .retrieve()
                .bodyToMono(TransactionResponse.class);
    }

    public Mono<TransactionResponse> withdraw(String accountId, BigDecimal amount, String idempotencyKey) {

        return webClient.post().uri("/accounts/{accountId}/withdraw", accountId)
                .header("Idempotency-Key", idempotencyKey)
                .bodyValue(new MoneyRequest(amount))
                .retrieve()
                .bodyToMono(TransactionResponse.class);
    }


}
