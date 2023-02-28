package com.kanwise.user_service.service.notification.email.implementation;

import com.kanwise.user_service.configuration.kafka.common.KafkaConfigurationProperties;
import com.kanwise.user_service.model.notification.email.EmailRequest;
import com.kanwise.user_service.service.notification.email.IEmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.kanwise.user_service.model.kafka.TopicType.NOTIFICATION_EMAIL;

@RequiredArgsConstructor
@Service
public class EmailNotificationService implements IEmailNotificationService<EmailRequest> {

    private final KafkaTemplate<String, EmailRequest> kafkaEmailTemplate;
    private final KafkaConfigurationProperties kafkaConfigurationProperties;

    @Override
    public void sendEmail(EmailRequest request) {
        kafkaEmailTemplate.send(kafkaConfigurationProperties.getTopicName(NOTIFICATION_EMAIL), request);
    }
}
