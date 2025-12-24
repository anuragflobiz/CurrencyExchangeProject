package com.CurrencyExchange.CurrencyExchangeProject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class sendMoneyDTO {
    private BigDecimal senderAmount;
    private UUID fromWalletId;
    private UUID toWalletId;;
}
