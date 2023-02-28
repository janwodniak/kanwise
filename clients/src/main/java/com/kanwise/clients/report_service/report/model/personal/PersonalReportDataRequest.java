package com.kanwise.clients.report_service.report.model.personal;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PersonalReportDataRequest(
        String username,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}