package com.CurrencyExchange.CurrencyExchangeProject.Service.Impl;

import com.CurrencyExchange.CurrencyExchangeProject.DTO.ExchangeRateResponse;
import com.CurrencyExchange.CurrencyExchangeProject.Service.FetchExchangeRateService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FetchExchangeRateServiceImpl implements FetchExchangeRateService {

    private static final String URL = "https://open.er-api.com/v6/latest/{base}";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, BigDecimal> getAllRates(String base) {

        try {
            ExchangeRateResponse response =
                    restTemplate.getForObject(URL, ExchangeRateResponse.class, base);

            if (response == null || response.getRates() == null) {
                throw new ExchangeRateFetchException("Empty exchange rate response");
            }

            return response.getRates()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> BigDecimal.valueOf(e.getValue())
                    ));

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to fetch exchange rates for base " + base, e
            );
        }
    }
}