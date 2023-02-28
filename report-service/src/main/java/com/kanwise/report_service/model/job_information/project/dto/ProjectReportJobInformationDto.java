package com.kanwise.report_service.model.job_information.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kanwise.report_service.model.job_information.common.JobStatus;
import com.kanwise.report_service.model.job_information.common.dto.JobInformationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class ProjectReportJobInformationDto extends JobInformationDto {
    private String id;
    private long projectId;
    private String subscriberUsername;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private JobStatus status;

    @Builder
    public ProjectReportJobInformationDto(String name,
                                          Integer totalFireCount,
                                          Integer remainingFireCount,
                                          Boolean runForever,
                                          Long repeatInterval,
                                          Long initialOffsetMs,
                                          String cron,
                                          String description,
                                          String id,
                                          long projectId,
                                          String subscriberUsername,
                                          LocalDateTime startDate,
                                          LocalDateTime endDate,
                                          JobStatus status) {
        super(name, totalFireCount, remainingFireCount, runForever, repeatInterval, initialOffsetMs, cron, description);
        this.id = id;
        this.projectId = projectId;
        this.subscriberUsername = subscriberUsername;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }
}
