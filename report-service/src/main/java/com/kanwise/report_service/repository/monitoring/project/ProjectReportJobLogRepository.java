package com.kanwise.report_service.repository.monitoring.project;

import com.kanwise.report_service.model.monitoring.project.ProjectReportJobLog;
import com.kanwise.report_service.repository.monitoring.common.JobLogRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectReportJobLogRepository extends JobLogRepository<ProjectReportJobLog> {
}
