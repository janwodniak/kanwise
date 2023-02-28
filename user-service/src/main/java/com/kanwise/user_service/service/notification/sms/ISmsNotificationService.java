package com.kanwise.user_service.service.notification.sms;

import com.kanwise.user_service.model.notification.sms.SmsRequest;

public interface ISmsNotificationService<T extends SmsRequest> {
    void sendSms(T request);
}
