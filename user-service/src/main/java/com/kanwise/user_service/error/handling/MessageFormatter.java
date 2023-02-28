package com.kanwise.user_service.error.handling;

public interface MessageFormatter {
    default String formatMessage(String message, boolean removePunctuation) {
        if (removePunctuation) {
            return message.toUpperCase().replaceAll("[.!?\\\\-]", "").replace("\s", "_");
        }
        return message.toUpperCase().replace("\s", "_");
    }
}
