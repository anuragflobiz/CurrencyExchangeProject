package com.CurrencyExchange.CurrencyExchangeProject.Service.Impl;

import com.coinShiftProject.coinShiftProject.DTO.NotificationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void send(NotificationDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);

            sqsTemplate.send(to -> to
                    .queue("coinshift-notification-queue")
                    .payload(json)
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to send SQS message", e);
        }
    }
}
