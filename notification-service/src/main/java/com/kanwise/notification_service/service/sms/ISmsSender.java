package com.kanwise.notification_service.service.sms;

import com.kanwise.clients.user_service.authentication.model.SmsResponse;
import com.kanwise.notification_service.model.sms.SmsRequest;
import reactor.core.publisher.Mono;

public interface ISmsSender<T extends SmsRequest, R extends SmsResponse> {

    Mono<R> sendSms(T smsRequest);
}
