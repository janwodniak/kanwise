package com.kanwise.report_service.model.job_information.personal;

import com.kanwise.report_service.model.job_information.common.JobInformation;
import com.kanwise.report_service.model.monitoring.personal.PersonalReportJobLog;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.FetchType.EAGER;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@SuperBuilder
@Where(clause = "active = true")
public class PersonalReportJobInformation extends JobInformation {
    @Builder.Default
    @OneToMany(mappedBy = "jobInformation", fetch = EAGER, cascade = MERGE)
    Set<PersonalReportJobLog> logs = new HashSet<>();
    private String username;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String email;

    public void addLog(PersonalReportJobLog log) {
        log.setJobInformation(this);
        logs.add(log);
    }
}
