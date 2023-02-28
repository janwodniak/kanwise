package com.kanwise.notification_service.service.email.sender;

import com.kanwise.notification_service.model.email.Email;

public interface IEmailSender {
    void send(Email email);
}
