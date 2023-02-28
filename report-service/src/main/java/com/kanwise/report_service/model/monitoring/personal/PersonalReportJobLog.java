package com.kanwise.report_service.model.monitoring.personal;

import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.monitoring.common.JobLog;
import com.kanwise.report_service.model.monitoring.common.LogStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.FetchType.EAGER;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@SuperBuilder
public class PersonalReportJobLog extends JobLog {
    @ManyToOne(cascade = {MERGE}, fetch = EAGER)
    private PersonalReportJobInformation jobInformation;
    private LogStatus status;
    private LocalDateTime timestamp;
    private String message;
}
