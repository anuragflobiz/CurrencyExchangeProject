package com.CurrencyExchange.CurrencyExchangeProject.Service.Impl;

import com.coinShiftProject.coinShiftProject.DTO.ExchangeRateResponse;
import com.coinShiftProject.coinShiftProject.Service.FetchExchangeRate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FetchExchangeRateImpl implements FetchExchangeRate {

    private static final String URL = "https://open.er-api.com/v6/latest/{base}";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, BigDecimal> getAllRates(String base) {

        ExchangeRateResponse response =
                restTemplate.getForObject(URL, ExchangeRateResponse.class, base);

        if (response == null || response.getRates() == null) {
            throw new RuntimeException("Exchange rate API failed");
        }

        return response.getRates()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> BigDecimal.valueOf(e.getValue())
                ));
    }
}