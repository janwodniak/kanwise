package com.kanwise.notification_service.service.email.sender.implementation;

import com.kanwise.notification_service.error.EmailSenderException;
import com.kanwise.notification_service.model.email.Email;
import com.kanwise.notification_service.service.email.sender.IEmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

import static org.apache.commons.codec.CharEncoding.UTF_8;


@RequiredArgsConstructor
@Service
public class EmailSender implements IEmailSender {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void send(Email email) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, UTF_8);
            helper.setText(email.content(), email.isHtml());
            helper.setTo(email.to());
            helper.setSubject(email.subject());
            mailSender.send(mimeMessage);
        } catch (Exception exception) {
            throw new EmailSenderException(exception.getMessage());
        }
    }
}
