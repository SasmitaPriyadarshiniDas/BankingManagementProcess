package org.natwest.transaction.controller;

import jakarta.validation.Valid;
import org.natwest.transaction.dto.request.TransactionRequest;
import org.natwest.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/transfers")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
        }

    @PostMapping
    public ResponseEntity<Void> transfer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransactionRequest request) {

        transactionService.transfer(request, idempotencyKey);

        return ResponseEntity.noContent().build();
    }
}
