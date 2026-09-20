package org.natwest.transaction.controller;

import jakarta.validation.Valid;
import org.natwest.transaction.dto.request.MoneyRequest;
import org.natwest.transaction.dto.request.TransactionRequest;
import org.natwest.transaction.dto.response.TransactionResponse;
import org.natwest.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
        }

    @PostMapping
    public ResponseEntity<Void> transfer(@RequestHeader("Idempotency-Key") String idempotencyKey, @Valid @RequestBody TransactionRequest request) {
        transactionService.transfer(request, idempotencyKey);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{accountId}/deposit")
    public Mono<ResponseEntity<TransactionResponse>> deposit(@PathVariable String accountId, @RequestHeader("Idempotency-Key") String idempotencyKey, @Valid @RequestBody MoneyRequest request) {
        return transactionService.deposit(accountId, request, idempotencyKey).map(ResponseEntity::ok);
    }
}
