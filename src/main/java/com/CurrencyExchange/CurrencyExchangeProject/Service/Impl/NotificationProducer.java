package com.CurrencyExchange.CurrencyExchangeProject.Service.Impl;

import com.CurrencyExchange.CurrencyExchangeProject.DTO.NotificationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${aws.sqs.notification-queue}")
    private String queueName;

    public void send(NotificationDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);

            sqsTemplate.send(to -> to
                    .queue(queueName)
                    .payload(json)
            );

        } catch (Exception e) {
            System.out.println("Failed to send notification to SQS: {}"+dto+" "+e);
        }
    }
}
