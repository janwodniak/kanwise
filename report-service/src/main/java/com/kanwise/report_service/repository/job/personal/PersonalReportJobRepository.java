package com.kanwise.report_service.repository.job.personal;

import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.repository.job.common.JobRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalReportJobRepository extends JobRepository<PersonalReportJobInformation> {
}
