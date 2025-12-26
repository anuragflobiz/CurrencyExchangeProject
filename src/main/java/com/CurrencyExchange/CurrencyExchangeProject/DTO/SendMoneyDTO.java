package com.CurrencyExchange.CurrencyExchangeProject.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMoneyDTO {
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal senderAmount;

    @NotNull(message = "From wallet id is required")
    private UUID fromWalletId;

    @NotNull(message = "To wallet id is required")
    private UUID toWalletId;
}
