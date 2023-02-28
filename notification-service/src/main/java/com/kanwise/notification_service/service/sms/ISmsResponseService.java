package com.kanwise.notification_service.service.sms;

import com.kanwise.clients.user_service.authentication.model.SmsResponse;

public interface ISmsResponseService<T extends SmsResponse> {
    void sendSmsResponse(T smsResponse);
}
