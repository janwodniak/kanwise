package com.kanwise.report_service.service.notification.email.common;


import com.kanwise.report_service.model.notification.email.EmailRequest;

public interface IEmailService {

    void sendEmail(EmailRequest emailRequest);
}
