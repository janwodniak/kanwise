package com.kanwise.notification_service.listeners.email;

import com.kanwise.notification_service.model.email.EmailRequest;

public interface IKafkaEmailListener<T extends EmailRequest> {
    void listener(T request);
}
