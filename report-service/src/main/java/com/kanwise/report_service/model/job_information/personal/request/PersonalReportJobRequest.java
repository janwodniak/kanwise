package com.kanwise.report_service.model.job_information.personal.request;

import com.kanwise.report_service.validation.annotation.cron.Cron;
import lombok.Builder;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record PersonalReportJobRequest(
        @NotBlank(message = "JOB_NAME_NOT_BLANK")
        String name,
        @Min(value = 0, message = "TOTAL_FIRE_COUNT_NOT_NEGATIVE")
        int totalFireCount,
        boolean runForever,
        @Min(value = 0, message = "REPEAT_INTERVAL_NOT_NEGATIVE")
        long repeatInterval,
        @Min(value = 0, message = "INITIAL_OFFSET_MS_NOT_NEGATIVE")
        long initialOffsetMs,
        @Cron
        String cron,
        @NotBlank(message = "USERNAME_NOT_BLANK")
        String username,

        @NotNull(message = "START_DATE_NOT_NULL")
        LocalDateTime startDate,
        @NotNull(message = "END_DATE_NOT_NULL")
        LocalDateTime endDate,
        Map<String, Object> data) {
}
