package com.CurrencyExchange.CurrencyExchangeProject.DTO;

import lombok.Data;

import java.util.Map;

@Data
public class ExchangeRateResponseDTO {
    private Map<String, Double> rates;
}
