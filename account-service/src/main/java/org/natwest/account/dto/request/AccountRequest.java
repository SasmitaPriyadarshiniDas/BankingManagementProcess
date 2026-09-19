package org.natwest.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AccountRequest(@NotBlank
                             String accountHolder,

                             @NotBlank
                             @Pattern(regexp = "[A-Z]{3}")
                             String currency) {
}
