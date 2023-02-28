package com.kanwise.notification_service.service.email.implementation;

import com.kanwise.notification_service.model.email.Email;
import com.kanwise.notification_service.model.email.EmailRequest;
import com.kanwise.notification_service.service.email.IEmailService;
import com.kanwise.notification_service.service.email.sender.IEmailSender;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailService implements IEmailService {
    private final IEmailSender emailSender;
    private final ModelMapper modelMapper;

    @Override
    public void sendEmail(EmailRequest emailRequest) {
        Email email = modelMapper.map(emailRequest, Email.class);
        emailSender.send(email);
    }
}
