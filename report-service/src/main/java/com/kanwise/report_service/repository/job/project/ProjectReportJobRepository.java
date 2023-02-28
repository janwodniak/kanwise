package com.kanwise.report_service.repository.job.project;

import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.repository.job.common.JobRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectReportJobRepository extends JobRepository<ProjectReportJobInformation> {
}
