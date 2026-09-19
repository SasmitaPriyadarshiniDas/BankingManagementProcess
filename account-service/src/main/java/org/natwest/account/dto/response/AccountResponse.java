package org.natwest.account.dto.response;

import java.math.BigDecimal;

public record AccountResponse(String accountId,
                              String accountHolder,
                              BigDecimal balance,
                              String currency,
                              String status) {
}
