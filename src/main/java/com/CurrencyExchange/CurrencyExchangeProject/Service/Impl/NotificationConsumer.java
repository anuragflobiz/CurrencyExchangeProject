package com.CurrencyExchange.CurrencyExchangeProject.Service.Impl;

import com.CurrencyExchange.CurrencyExchangeProject.DTO.NotificationDTO;
import com.CurrencyExchange.CurrencyExchangeProject.Enums.CurrencyCode;
import com.CurrencyExchange.CurrencyExchangeProject.Service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    @Autowired
    private EmailService emailService;

    @SqsListener("coinshift-notification-queue")
    public void consume(String raw) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        NotificationDTO dto = mapper.readValue(raw, NotificationDTO.class);
        switch (dto.getType()) {
            case "CREDIT" -> emailService.creditMail(
                    dto.getEmail(),
                    dto.getAmount(),
                    Enum.valueOf(CurrencyCode.class, dto.getCurrency()),
                    dto.getTime(),
                    dto.getBalance()
            );
            case "DEBIT" -> emailService.debitMail(
                    dto.getEmail(),
                    dto.getAmount(),
                    Enum.valueOf(CurrencyCode.class, dto.getCurrency()),
                    dto.getTime(),
                    dto.getBalance()
            );

        }
    }
}

