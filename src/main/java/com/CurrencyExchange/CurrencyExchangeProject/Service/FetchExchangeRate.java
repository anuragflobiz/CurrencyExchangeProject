package com.CurrencyExchange.CurrencyExchangeProject.Service;

import java.math.BigDecimal;
import java.util.Map;

public interface FetchExchangeRate {
    public Map<String, BigDecimal> getAllRates(String base);
}
