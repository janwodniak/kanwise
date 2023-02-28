package com.kanwise.notification_service.service.sms.otp;

import com.kanwise.clients.user_service.authentication.model.OtpSmsResponse;
import com.kanwise.notification_service.configuration.twillio.TwilioConfigurationProperties;
import com.kanwise.notification_service.model.sms.OtpSmsRequest;
import com.kanwise.notification_service.service.sms.ISmsSender;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.kanwise.clients.user_service.authentication.model.OtpStatus.DELIVERED;
import static com.kanwise.clients.user_service.authentication.model.OtpStatus.FAILED;
import static com.kanwise.notification_service.constants.SmsConstant.SMS_DELIVERED_SUCCESSFULLY_MESSAGE;
import static reactor.core.publisher.Mono.just;

@RequiredArgsConstructor
@Service
public class OtpSmsService implements ISmsSender<OtpSmsRequest, OtpSmsResponse> {

    private final TwilioConfigurationProperties twilioConfigurationProperties;

    @Override
    public Mono<OtpSmsResponse> sendSms(OtpSmsRequest request) {
        OtpSmsResponse response;
        try {
            getMessage(request).create();
            response = generateSuccessOtpSmsResponse(request);
        } catch (Exception exception) {
            response = generateFailureOtpSmsResponse(request, exception.getMessage());
        }
        return just(response);
    }

    private OtpSmsResponse generateSuccessOtpSmsResponse(OtpSmsRequest request) {
        return OtpSmsResponse.builder()
                .otpId(request.getOtpId())
                .status(DELIVERED)
                .message(SMS_DELIVERED_SUCCESSFULLY_MESSAGE)
                .build();
    }

    private OtpSmsResponse generateFailureOtpSmsResponse(OtpSmsRequest request, String exceptionMessage) {
        return OtpSmsResponse.builder()
                .otpId(request.getOtpId())
                .status(FAILED)
                .message(exceptionMessage)
                .build();
    }

    private MessageCreator getMessage(OtpSmsRequest request) {
        PhoneNumber recipient = new PhoneNumber(request.getPhoneNumber());
        PhoneNumber sender = new PhoneNumber(twilioConfigurationProperties.number());
        String content = request.getContent();
        return Message.creator(recipient, sender, content);
    }
}
