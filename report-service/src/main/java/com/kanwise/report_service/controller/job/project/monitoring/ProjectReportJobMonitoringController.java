package com.kanwise.report_service.controller.job.project.monitoring;

import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.model.monitoring.project.ProjectReportJobLog;
import com.kanwise.report_service.model.monitoring.project.dto.ProjectReportJobLogDto;
import com.kanwise.report_service.service.job_information.monitoring.common.MonitoringService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/job/report/project")
public class ProjectReportJobMonitoringController {

    private final MonitoringService<ProjectReportJobLog, ProjectReportJobInformation> projectReportJobMonitoringService;

    private final ModelMapper modelMapper;

    @ApiOperation(value = "Get logs for project report job",
            notes = "Get logs for project report job",
            response = ProjectReportJobLogDto.class,
            responseReference = "ResponseEntity<Set<ProjectReportJobLogDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @GetMapping("{id}/logs")
    public ResponseEntity<Set<ProjectReportJobLogDto>> getJobLogs(@PathVariable String id) {
        Set<ProjectReportJobLog> logs = projectReportJobMonitoringService.getLogs(id);
        return ResponseEntity.ok(logs.stream().map(log -> modelMapper.map(log, ProjectReportJobLogDto.class)).collect(toSet()));
    }
}
