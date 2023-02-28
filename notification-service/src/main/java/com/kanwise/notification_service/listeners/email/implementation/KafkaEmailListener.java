package com.kanwise.notification_service.listeners.email.implementation;

import com.kanwise.notification_service.listeners.email.IKafkaEmailListener;
import com.kanwise.notification_service.model.email.EmailRequest;
import com.kanwise.notification_service.service.email.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class KafkaEmailListener implements IKafkaEmailListener<EmailRequest> {

    private final IEmailService emailService;

    @Override
    @KafkaListener(topics = "notification-email", groupId = "mail_senders", containerFactory = "emailFactory")
    public void listener(EmailRequest emailRequest) {
        emailService.sendEmail(emailRequest);
    }
}
