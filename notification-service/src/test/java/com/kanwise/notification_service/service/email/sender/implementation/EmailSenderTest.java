package com.kanwise.notification_service.service.email.sender.implementation;

import com.kanwise.notification_service.error.EmailSenderException;
import com.kanwise.notification_service.model.email.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailSenderTest {

    private EmailSender emailSender;

    @Mock
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        emailSender = new EmailSender(mailSender);
    }

    @Test
    void shouldThrowEmailSendingExceptionWhenGivenEmail() {
        // Given
        Email email = Email.builder()
                .to("john.kanwise@gmail.com")
                .subject("Test")
                .content("Test")
                .isHtml(false)
                .build();
        // When
        when(mailSender.createMimeMessage()).thenThrow(MailParseException.class);
        // Then
        assertThrows(EmailSenderException.class, () -> emailSender.send(email));
    }
}