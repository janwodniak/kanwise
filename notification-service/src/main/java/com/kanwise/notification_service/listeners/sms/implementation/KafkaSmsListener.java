package com.kanwise.notification_service.listeners.sms.implementation;

import com.kanwise.clients.user_service.authentication.model.OtpSmsResponse;
import com.kanwise.notification_service.listeners.sms.IKafkaSmsListener;
import com.kanwise.notification_service.model.sms.OtpSmsRequest;
import com.kanwise.notification_service.service.sms.ISmsResponseService;
import com.kanwise.notification_service.service.sms.ISmsSender;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class KafkaSmsListener implements IKafkaSmsListener<OtpSmsRequest> {

    private final ISmsSender<OtpSmsRequest, OtpSmsResponse> otpSmsService;
    private final ISmsResponseService<OtpSmsResponse> otpSmsResponseService;

    @Override
    @KafkaListener(topics = "notification-sms", groupId = "sms_senders", containerFactory = "smsFactory")
    public void listener(OtpSmsRequest request) {
        otpSmsService.sendSms(request).subscribe(otpSmsResponseService::sendSmsResponse);
    }
}
