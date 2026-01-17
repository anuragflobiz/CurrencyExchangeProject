package com.CurrencyExchange.CurrencyExchangeProject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private String type;
    private String email;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime time;
    private BigDecimal balance;

}
