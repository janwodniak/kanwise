package com.kanwise.report_service.model.job_information.project.request;

import com.kanwise.report_service.validation.annotation.cron.Cron;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

public record ProjectReportJobRequest(
        @NotEmpty(message = "JOB_NAME_NOT_EMPTY")
        String name,
        @Min(value = 0, message = "TOTAL_FIRE_COUNT_NOT_NEGATIVE")
        int totalFireCount,
        boolean runForever,
        @Min(value = 0, message = "REPEAT_INTERVAL_NOT_NEGATIVE")
        long repeatInterval,
        @Min(value = 0, message = "INITIAL_OFFSET_NOT_NEGATIVE")
        long initialOffsetMs,
        @Cron
        String cron,
        @NotEmpty(message = "USERNAME_NOT_EMPTY")
        String username,
        @NotNull(message = "PROJECT_ID_NOT_NULL")
        Long projectId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Map<String, Object> data
) {
}
