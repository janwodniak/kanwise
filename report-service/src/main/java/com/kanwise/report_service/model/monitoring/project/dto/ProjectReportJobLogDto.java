package com.kanwise.report_service.model.monitoring.project.dto;

import com.kanwise.report_service.model.monitoring.common.LogStatus;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class ProjectReportJobLogDto extends RepresentationModel<ProjectReportJobLogDto> {
    Long id;
    String jobId;
    LogStatus status;
    LocalDateTime timestamp;
    String message;
    String subscriberUsername;
    Map<String, String> data;
}
