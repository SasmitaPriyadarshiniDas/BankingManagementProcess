package org.natwest.transaction.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(@NotBlank(message = "From account ID is required") String fromAccountId,

                                 @NotBlank(message = "To account ID is required") String toAccountId,

                                 @NotNull(message = "Amount is required")
                                 @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
                                 BigDecimal amount) {
}
