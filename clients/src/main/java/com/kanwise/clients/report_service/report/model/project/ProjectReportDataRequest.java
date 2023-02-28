package com.kanwise.clients.report_service.report.model.project;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ProjectReportDataRequest(
        Long projectId,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
