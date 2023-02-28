package com.kanwise.user_service.service.notification.sms.implementation;

import com.kanwise.user_service.configuration.kafka.common.KafkaConfigurationProperties;
import com.kanwise.user_service.model.notification.sms.OtpSmsRequest;
import com.kanwise.user_service.service.notification.sms.ISmsNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.kanwise.user_service.model.kafka.TopicType.NOTIFICATION_SMS;

@RequiredArgsConstructor
@Service
public class OtpSmsNotificationService implements ISmsNotificationService<OtpSmsRequest> {

    private final KafkaTemplate<String, OtpSmsRequest> kafkaSmsTemplate;
    private final KafkaConfigurationProperties kafkaConfigurationProperties;

    @Override
    public void sendSms(OtpSmsRequest request) {
        kafkaSmsTemplate.send(kafkaConfigurationProperties.getTopicName(NOTIFICATION_SMS), request);
    }
}
