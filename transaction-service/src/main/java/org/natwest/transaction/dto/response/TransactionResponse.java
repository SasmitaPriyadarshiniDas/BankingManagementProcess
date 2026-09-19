package org.natwest.transaction.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(UUID transactionId,
                                  String accountId,
                                  String transactionType,
                                  BigDecimal amount,
                                  BigDecimal balanceAfter,
                                  String referenceId,
                                  Instant createdAt) {
}
