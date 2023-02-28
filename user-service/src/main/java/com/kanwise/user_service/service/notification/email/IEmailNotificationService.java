package com.kanwise.user_service.service.notification.email;

import com.kanwise.user_service.model.notification.email.EmailRequest;

public interface IEmailNotificationService<T extends EmailRequest> {
    void sendEmail(T request);
}
