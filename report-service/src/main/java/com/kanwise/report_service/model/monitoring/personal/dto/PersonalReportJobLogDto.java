package com.kanwise.report_service.model.monitoring.personal.dto;

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
public class PersonalReportJobLogDto extends RepresentationModel<PersonalReportJobLogDto> {
    Long id;
    String subscriberUsername;
    String jobId;
    LogStatus status;
    LocalDateTime timestamp;
    String message;
    Map<String, String> data;
}
