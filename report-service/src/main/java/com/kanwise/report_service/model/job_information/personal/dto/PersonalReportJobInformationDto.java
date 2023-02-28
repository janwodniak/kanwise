package com.kanwise.report_service.model.job_information.personal.dto;

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
public class PersonalReportJobInformationDto extends JobInformationDto {
    private String id;
    private String subscriberUsername;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private JobStatus status;

    @Builder
    public PersonalReportJobInformationDto(String name,
                                           Integer totalFireCount,
                                           Integer remainingFireCount,
                                           Boolean runForever,
                                           Long repeatInterval,
                                           Long initialOffsetMs,
                                           String cron,
                                           String description,
                                           String id,
                                           String subscriberUsername,
                                           LocalDateTime startDate,
                                           LocalDateTime endDate,
                                           JobStatus status) {
        super(name, totalFireCount, remainingFireCount, runForever, repeatInterval, initialOffsetMs, cron, description);
        this.id = id;
        this.subscriberUsername = subscriberUsername;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }
}
