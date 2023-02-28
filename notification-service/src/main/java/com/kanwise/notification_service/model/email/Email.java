package com.kanwise.notification_service.model.email;

import lombok.Builder;

@Builder
public record Email(
        String to,
        String subject,
        String content,
        boolean isHtml
) {
}
