package org.natwest.account.config;

import org.natwest.account.dto.request.MoneyRequest;
import org.natwest.account.dto.response.BalanceResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TransactionClient {

    private final WebClient webClient;

    public TransactionClient(WebClient webClient) {
        this.webClient = webClient;
    }
    public Mono<BalanceResponse> deposit(String accountId, MoneyRequest request, String idempotencyKey) {

        return webClient.post()
                .uri("/transactions/{accountId}/deposit", accountId)
                .header("Idempotency-Key", idempotencyKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(BalanceResponse.class);
    }

    public Mono<BalanceResponse> withdraw(String accountId, MoneyRequest request, String idempotencyKey) {

        return webClient.post()
                .uri("/transactions/{accountId}/withdraw", accountId)
                .header("Idempotency-Key", idempotencyKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(BalanceResponse.class);
    }

}
