package com.kanwise.report_service.service.notification.email.implementation;


import com.kanwise.report_service.configuration.kafka.common.KafkaConfigurationProperties;
import com.kanwise.report_service.model.notification.email.EmailRequest;
import com.kanwise.report_service.service.notification.email.common.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.kanwise.report_service.model.kafka.TopicType.NOTIFICATION_EMAIL;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService implements IEmailService {

    private final KafkaTemplate<String, EmailRequest> kafkaEmailTemplate;
    private final KafkaConfigurationProperties kafkaConfigurationProperties;

    @Override
    public void sendEmail(EmailRequest messageRequest) {
        kafkaEmailTemplate.send(kafkaConfigurationProperties.getTopicName(NOTIFICATION_EMAIL), messageRequest);
    }
}
