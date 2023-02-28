package com.kanwise.report_service.repository.monitoring.personal;

import com.kanwise.report_service.model.monitoring.personal.PersonalReportJobLog;
import com.kanwise.report_service.repository.monitoring.common.JobLogRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalReportJobLogRepository extends JobLogRepository<PersonalReportJobLog> {

}
