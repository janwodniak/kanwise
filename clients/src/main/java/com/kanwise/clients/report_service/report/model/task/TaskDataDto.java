package com.kanwise.clients.report_service.report.model.task;

import liquibase.repackaged.org.apache.commons.lang3.time.DurationFormatUtils;
import lombok.Builder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
public record TaskDataDto(
        String title,
        String projectTitle,
        LocalDateTime createdAt,
        Duration estimatedTime,
        Duration actualTime,
        long performance
) {

    public String getCreatedAtAsString() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return createdAt.format(dateTimeFormatter);
    }

    public String getCreatedAtAsString(String pattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return createdAt.format(dateTimeFormatter);
    }

    public String getEstimatedTimeAsString() {
        return DurationFormatUtils.formatDuration(estimatedTime.toMillis(), "HH:mm");
    }

    public String getEstimatedTimeAsString(String pattern) {
        return DurationFormatUtils.formatDuration(estimatedTime.toMillis(), pattern);
    }

    public String getActualTimeAsString(String pattern) {
        return DurationFormatUtils.formatDuration(actualTime.toMillis(), pattern);
    }

    public String getActualTimeAsString() {
        return DurationFormatUtils.formatDuration(actualTime.toMillis(), "HH:mm");
    }
}
