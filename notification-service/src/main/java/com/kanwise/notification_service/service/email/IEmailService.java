package com.kanwise.notification_service.service.email;

import com.kanwise.notification_service.model.email.EmailRequest;

public interface IEmailService {
    void sendEmail(EmailRequest emailRequest);
}
