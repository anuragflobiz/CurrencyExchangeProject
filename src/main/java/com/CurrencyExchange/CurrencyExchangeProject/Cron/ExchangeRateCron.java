package com.CurrencyExchange.CurrencyExchangeProject.Cron;

import com.CurrencyExchange.CurrencyExchange.Service.FetchExchangeRate;
import com.CurrencyExchange.CurrencyExchange.enums.CurrencyCode;
import com.CurrencyExchange.CurrencyExchangeProject.Service.FetchExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Component
public class ExchangeRateCron {
    @Autowired
    private FetchExchangeRateService exchangeRateService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 */15 * * * *")
    public void updateExchangeRates() {
        try {
            List<CurrencyCode> currencies = List.of(CurrencyCode.values());
            for (CurrencyCode base : currencies) {
                Map<String, BigDecimal> rates = exchangeRateService.getAllRates(base.name());
                for (CurrencyCode target : currencies) {
                    if (base == target) continue;
                    BigDecimal rate = rates.get(target.name());
                    if (rate == null) continue;
                    redisTemplate.opsForValue().set(
                            "RATE:" + base + ":" + target,
                            rate.toPlainString()
                    );
                    redisTemplate.opsForValue().set(
                            "RATE:" + target + ":" + base,
                            BigDecimal.ONE.divide(rate, 8, RoundingMode.HALF_UP)
                                    .toPlainString()
                    );
                }
            }
            System.out.println("Exchange rates updated successfully");
        } catch (Exception e) {
            System.err.println("Exchange rate cron failed: " + e.getMessage());
        }
    }

}

