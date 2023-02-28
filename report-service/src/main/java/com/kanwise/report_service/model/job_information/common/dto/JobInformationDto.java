package com.kanwise.report_service.model.job_information.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class JobInformationDto extends RepresentationModel<JobInformationDto> {
    private String name;
    private Integer totalFireCount;
    private Integer remainingFireCount;
    private Boolean runForever;
    private Long repeatInterval;
    private Long initialOffsetMs;
    private String cron;
    private String description;
}
