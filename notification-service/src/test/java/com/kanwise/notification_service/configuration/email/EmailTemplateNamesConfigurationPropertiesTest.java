package com.kanwise.notification_service.configuration.email;

import com.kanwise.notification_service.NotificationServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.kanwise.notification_service.model.email.EmailMessageType.ACCOUNT_BLOCKED;
import static com.kanwise.notification_service.model.email.EmailMessageType.ACCOUNT_CREATED;
import static com.kanwise.notification_service.model.email.EmailMessageType.NEW_TASK_ASSIGNED;
import static com.kanwise.notification_service.model.email.EmailMessageType.PASSWORD_CHANGED;
import static com.kanwise.notification_service.model.email.EmailMessageType.PASSWORD_RESET;
import static com.kanwise.notification_service.model.email.EmailMessageType.PERSONAL_REPORT;
import static com.kanwise.notification_service.model.email.EmailMessageType.PROJECT_JOIN_REQUEST_ACCEPTED;
import static com.kanwise.notification_service.model.email.EmailMessageType.PROJECT_JOIN_REQUEST_REJECTED;
import static com.kanwise.notification_service.model.email.EmailMessageType.PROJECT_REPORT;
import static com.kanwise.notification_service.model.email.EmailMessageType.USER_INFORMATION_CHANGED;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = NotificationServiceApplication.class)
@ActiveProfiles("test-kafka-disabled")
class EmailTemplateNamesConfigurationPropertiesTest {

    @Autowired
    private EmailTemplateNamesConfigurationProperties emailTemplateNamesConfigurationProperties;

    @Test
    void shouldNames() {
        // Given
        // When
        // Then
        assertEquals("account-created.html", emailTemplateNamesConfigurationProperties.names().get(ACCOUNT_CREATED));
        assertEquals("account-blocked.html", emailTemplateNamesConfigurationProperties.names().get(ACCOUNT_BLOCKED));
        assertEquals("project-join-request-rejected.html", emailTemplateNamesConfigurationProperties.names().get(PROJECT_JOIN_REQUEST_REJECTED));
        assertEquals("project-join-request-accepted.html", emailTemplateNamesConfigurationProperties.names().get(PROJECT_JOIN_REQUEST_ACCEPTED));
        assertEquals("new-task-assigned.html", emailTemplateNamesConfigurationProperties.names().get(NEW_TASK_ASSIGNED));
        assertEquals("password-reset.html", emailTemplateNamesConfigurationProperties.names().get(PASSWORD_RESET));
        assertEquals("password-changed.html", emailTemplateNamesConfigurationProperties.names().get(PASSWORD_CHANGED));
        assertEquals("user-information-changed.html", emailTemplateNamesConfigurationProperties.names().get(USER_INFORMATION_CHANGED));
        assertEquals("personal-report.html", emailTemplateNamesConfigurationProperties.names().get(PERSONAL_REPORT));
        assertEquals("project-report.html", emailTemplateNamesConfigurationProperties.names().get(PROJECT_REPORT));
    }
}