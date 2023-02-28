package com.kanwise.notification_service.configuration.twillio;

import com.twilio.Twilio;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioInitializer {

    public TwilioInitializer(TwilioConfigurationProperties twilioConfigurationProperties) {
        Twilio.init(twilioConfigurationProperties.accountSid(), twilioConfigurationProperties.authToken());
    }
}
