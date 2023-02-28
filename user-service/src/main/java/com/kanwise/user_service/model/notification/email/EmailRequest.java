package com.kanwise.user_service.model.notification.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
    private String to;
    private String subject;
    private EmailMessageType type;
    private Map<String, Object> data;
    private boolean isHtml;
}
