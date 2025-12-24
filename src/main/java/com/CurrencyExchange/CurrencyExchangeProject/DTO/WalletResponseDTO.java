package com.CurrencyExchange.CurrencyExchangeProject.DTO;

import com.coinShiftProject.coinShiftProject.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletResponseDTO {
    private UUID id;
    private CurrencyCode currencyCode;
    private BigDecimal balance;
}