package com.kanwise.notification_service.listeners.sms;

import com.kanwise.notification_service.model.sms.SmsRequest;

public interface IKafkaSmsListener<T extends SmsRequest> {
    void listener(T request);
}
