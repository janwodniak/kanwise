package com.kanwise.report_service.service.response.common;

public interface IResponseMessageFormatter {
    default String replace(String message, String regex, String replacement) {
        return message.replaceAll(regex, replacement);
    }
}
