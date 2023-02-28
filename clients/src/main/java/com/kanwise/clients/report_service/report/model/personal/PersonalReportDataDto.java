package com.kanwise.clients.report_service.report.model.personal;

import lombok.Builder;

import java.util.Map;

@Builder
public record PersonalReportDataDto(Map<String, Object> data) {

}
