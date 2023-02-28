package com.kanwise.clients.report_service.report.client;

import com.kanwise.clients.report_service.report.model.personal.PersonalReportDataDto;
import com.kanwise.clients.report_service.report.model.personal.PersonalReportDataRequest;
import com.kanwise.clients.report_service.report.model.project.ProjectReportDataDto;
import com.kanwise.clients.report_service.report.model.project.ProjectReportDataRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "kanwise-service", path = "/report/data")
public interface ReportDataClient {

    @PostMapping("/personal")
    ResponseEntity<PersonalReportDataDto> getPersonalReportData(PersonalReportDataRequest request);

    @PostMapping("/project")
    ResponseEntity<ProjectReportDataDto> getProjectReportData(ProjectReportDataRequest request);
}
