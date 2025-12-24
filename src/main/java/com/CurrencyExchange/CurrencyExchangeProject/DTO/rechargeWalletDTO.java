package com.CurrencyExchange.CurrencyExchangeProject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class rechargeWalletDTO {
    private BigDecimal amount;
    private UUID walletid;

}
