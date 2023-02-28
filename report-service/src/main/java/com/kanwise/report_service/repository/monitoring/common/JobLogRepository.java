package com.kanwise.report_service.repository.monitoring.common;

import com.kanwise.report_service.model.monitoring.common.JobLog;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Scope("prototype")
public interface JobLogRepository<T extends JobLog> extends JpaRepository<T, Long> {
}
