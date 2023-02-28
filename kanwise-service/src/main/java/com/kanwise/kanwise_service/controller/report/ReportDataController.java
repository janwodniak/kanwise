package com.kanwise.kanwise_service.controller.report;

import com.kanwise.clients.report_service.report.model.personal.PersonalReportDataDto;
import com.kanwise.clients.report_service.report.model.personal.PersonalReportDataRequest;
import com.kanwise.clients.report_service.report.model.project.ProjectReportDataDto;
import com.kanwise.clients.report_service.report.model.project.ProjectReportDataRequest;
import com.kanwise.kanwise_service.model.report.personal.PersonalReportData;
import com.kanwise.kanwise_service.model.report.project.ProjectReportData;
import com.kanwise.kanwise_service.service.report.ReportDataService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static javax.ws.rs.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RequestMapping("/report/data")
@RestController
public class ReportDataController {

    private final ReportDataService<PersonalReportData, PersonalReportDataRequest> personalReportDataService;
    private final ReportDataService<ProjectReportData, ProjectReportDataRequest> projectReportDataService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Get personal report data",
            notes = "This endpoint is used to get personal report data.",
            response = PersonalReportDataDto.class,
            responseReference = "ResponseEntity<PersonalReportDataDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PostMapping("/personal")
    public ResponseEntity<PersonalReportDataDto> getPersonalReportData(@Valid @RequestBody PersonalReportDataRequest request) {
        PersonalReportData data = personalReportDataService.getReportData(request);
        return ResponseEntity.ok(modelMapper.map(data, PersonalReportDataDto.class));
    }

    @ApiOperation(value = "Get project report data",
            notes = "This endpoint is used to get project report data.",
            response = ProjectReportDataDto.class,
            responseReference = "ResponseEntity<ProjectReportDataDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PostMapping("/project")
    public ResponseEntity<ProjectReportDataDto> getProjectReportData(@Valid @RequestBody ProjectReportDataRequest request) {
        ProjectReportData data = projectReportDataService.getReportData(request);
        return ResponseEntity.ok(modelMapper.map(data, ProjectReportDataDto.class));
    }
}
