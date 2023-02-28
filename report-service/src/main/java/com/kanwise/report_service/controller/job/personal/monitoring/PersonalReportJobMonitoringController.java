package com.kanwise.report_service.controller.job.personal.monitoring;

import com.kanwise.report_service.error.handling.ExceptionHandling;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.monitoring.personal.PersonalReportJobLog;
import com.kanwise.report_service.model.monitoring.personal.dto.PersonalReportJobLogDto;
import com.kanwise.report_service.service.job_information.monitoring.common.MonitoringService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
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
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping("/job/report/personal")
public class PersonalReportJobMonitoringController extends ExceptionHandling {

    private final MonitoringService<PersonalReportJobLog, PersonalReportJobInformation> personalReportJobMonitoringService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Get logs for personal report job",
            notes = "Get logs for personal report job",
            response = PersonalReportJobLogDto.class,
            responseReference = "ResponseEntity<Set<PersonalReportJobLogDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @GetMapping("{id}/logs")
    public ResponseEntity<Set<PersonalReportJobLogDto>> getJobLogs(@PathVariable String id) {
        Set<PersonalReportJobLog> logs = personalReportJobMonitoringService.getLogs(id);
        return new ResponseEntity<>(logs.stream().map(log -> modelMapper.map(log, PersonalReportJobLogDto.class)).collect(toSet()), OK);
    }
}
