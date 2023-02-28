package com.kanwise.kanwise_service.service.response;

public interface IResponseMessageFormatter {
    default String replace(String message, String regex, String replacement) {
        return message.replaceAll(regex, replacement);
    }
}
