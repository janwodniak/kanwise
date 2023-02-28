package com.kanwise.report_service.controller.job.project;

import com.kanwise.report_service.model.job_information.personal.dto.PersonalReportJobInformationDto;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.model.job_information.project.dto.ProjectReportJobInformationDto;
import com.kanwise.report_service.model.job_information.project.request.ProjectReportJobRequest;
import com.kanwise.report_service.service.job.common.JobService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping("/job/report/project")
public class ProjectReportJobController {

    private final JobService<ProjectReportJobInformation> projectReportJobService;

    private final ModelMapper modelMapper;

    @ApiOperation(value = "Create project report job",
            notes = "Create project report job",
            response = ProjectReportJobInformationDto.class,
            responseReference = "ResponseEntity<ProjectReportJobInformationDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping
    public ResponseEntity<ProjectReportJobInformationDto> runJob(@RequestBody @Valid ProjectReportJobRequest request) {
        ProjectReportJobInformation jobInfo = projectReportJobService.runJob(modelMapper.map(request, ProjectReportJobInformation.class));
        return new ResponseEntity<>(modelMapper.map(jobInfo, ProjectReportJobInformationDto.class), CREATED);
    }

    @ApiOperation(value = "Get project report job by id",
            notes = "Get project report job by id",
            response = ProjectReportJobInformationDto.class,
            responseReference = "ResponseEntity<ProjectReportJobInformationDto>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectReportJobInformationDto> getJob(@PathVariable String id) {
        ProjectReportJobInformation jobInfo = projectReportJobService.getJob(id);
        return new ResponseEntity<>(modelMapper.map(jobInfo, ProjectReportJobInformationDto.class), OK);
    }

    @ApiOperation(value = "Get project report jobs",
            notes = "Get project report jobs",
            response = ProjectReportJobInformationDto.class,
            responseReference = "ResponseEntity<List<ProjectReportJobInformationDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @GetMapping
    public ResponseEntity<List<ProjectReportJobInformationDto>> getJobs() {
        List<ProjectReportJobInformation> jobs = projectReportJobService.getAllJobs();
        return new ResponseEntity<>(jobs.stream().map(job -> modelMapper.map(job, ProjectReportJobInformationDto.class)).toList(), OK);
    }

    @ApiOperation(value = "DELETE project report job by id",
            notes = "DELETE project report job by id",
            response = HttpStatus.class,
            responseReference = "ResponseEntity<HttpStatus>",
            httpMethod = DELETE,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteJob(@PathVariable String id) {
        projectReportJobService.deleteJob(id);
        return new ResponseEntity<>(NO_CONTENT);
    }

    @ApiOperation(value = "Resume personal report job by id",
            notes = "Resume personal report job by id",
            response = PersonalReportJobInformationDto.class,
            responseReference = "ResponseEntity<PersonalReportJobInformationDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping("{id}/stop")
    public ResponseEntity<ProjectReportJobInformationDto> stopJob(@PathVariable String id) {
        ProjectReportJobInformation jobInfo = projectReportJobService.stopJob(id);
        return new ResponseEntity<>(modelMapper.map(jobInfo, ProjectReportJobInformationDto.class), OK);
    }

    @ApiOperation(value = "Resume project report job by id",
            notes = "Resume project report job by id",
            response = ProjectReportJobInformationDto.class,
            responseReference = "ResponseEntity<PersonalReportJobInformationDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping("{id}/restart")
    public ResponseEntity<ProjectReportJobInformationDto> restartJob(@PathVariable String id) {
        ProjectReportJobInformation jobInfo = projectReportJobService.restartJob(id);
        return new ResponseEntity<>(modelMapper.map(jobInfo, ProjectReportJobInformationDto.class), OK);
    }
}
