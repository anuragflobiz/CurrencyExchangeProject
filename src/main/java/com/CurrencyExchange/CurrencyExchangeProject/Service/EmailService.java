package com.CurrencyExchange.CurrencyExchangeProject.Service;

import com.CurrencyExchange.CurrencyExchangeProject.Enums.CurrencyCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public interface EmailService {

    public void sendOtp(String toEmail,String otp);

    public void creditMail(String toEmail, BigDecimal amount, CurrencyCode currencyCode, LocalDateTime time, BigDecimal finalAmount);

    public void debitMail(String toEmail, BigDecimal amount, CurrencyCode currencyCode, LocalDateTime time,BigDecimal finalAmount);


}
