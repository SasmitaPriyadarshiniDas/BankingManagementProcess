package org.natwest.account.dto.response;

import java.math.BigDecimal;

public record BalanceResponse(String accountId, BigDecimal balance, String currency) {
}
