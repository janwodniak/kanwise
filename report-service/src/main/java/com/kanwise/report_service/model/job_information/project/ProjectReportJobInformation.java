package com.kanwise.report_service.model.job_information.project;

import com.kanwise.report_service.model.job_information.common.JobInformation;
import com.kanwise.report_service.model.monitoring.project.ProjectReportJobLog;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

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
public class ProjectReportJobInformation extends JobInformation {
    @Builder.Default
    @OneToMany(mappedBy = "jobInformation", fetch = EAGER, cascade = MERGE)
    Set<ProjectReportJobLog> logs = new HashSet<>();
    private long projectId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String email;
    private String username;

    public void addLog(ProjectReportJobLog log) {
        log.setJobInformation(this);
        logs.add(log);
    }
}
