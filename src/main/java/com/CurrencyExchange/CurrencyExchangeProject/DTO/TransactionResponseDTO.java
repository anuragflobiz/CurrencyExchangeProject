package com.CurrencyExchange.CurrencyExchangeProject.DTO;


import com.CurrencyExchange.CurrencyExchangeProject.Enums.CurrencyCode;
import com.CurrencyExchange.CurrencyExchangeProject.Enums.PaymentStatus;
import com.CurrencyExchange.CurrencyExchangeProject.Enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDTO {
    private String counterPartyName;
    private BigDecimal senderAmount;
    private BigDecimal receiverAmount;
    private PaymentStatus paymentStatus;
    private TransactionType transactionType;
    private CurrencyCode senderCurrency;
    private CurrencyCode receiverCurrency;
    private BigDecimal exchangeRate;
    private BigDecimal fees;
    private LocalDateTime createdAt;
    private String direction;
}
