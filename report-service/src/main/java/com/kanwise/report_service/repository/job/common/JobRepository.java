package com.kanwise.report_service.repository.job.common;

import com.kanwise.report_service.model.job_information.common.JobInformation;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Scope("prototype")
public interface JobRepository<T extends JobInformation> extends JpaRepository<T, String> {
}
