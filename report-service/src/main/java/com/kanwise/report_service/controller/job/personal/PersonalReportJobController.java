package com.kanwise.report_service.controller.job.personal;


import com.kanwise.report_service.error.handling.ExceptionHandling;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.job_information.personal.dto.PersonalReportJobInformationDto;
import com.kanwise.report_service.model.job_information.personal.request.PersonalReportJobRequest;
import com.kanwise.report_service.service.job.common.JobService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/job/report/personal")
public class PersonalReportJobController extends ExceptionHandling {

    private final JobService<PersonalReportJobInformation> personalReportJobService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Create personal report job",
            notes = "Create personal report job",
            response = PersonalReportJobInformationDto.class,
            responseReference = "ResponseEntity<PersonalReportJobInformationDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping
    public ResponseEntity<PersonalReportJobInformationDto> runJob(@RequestBody @Valid PersonalReportJobRequest request) {
        PersonalReportJobInformation jobInfo = personalReportJobService.runJob(modelMapper.map(request, PersonalReportJobInformation.class));
        return new ResponseEntity<>(modelMapper.map(jobInfo, PersonalReportJobInformationDto.class), CREATED);
    }

    @ApiOperation(value = "Get all personal report jobs",
            notes = "Get all personal report jobs",
            response = PersonalReportJobInformationDto.class,
            responseReference = "ResponseEntity<List<PersonalReportJobInformationDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @GetMapping
    public ResponseEntity<List<PersonalReportJobInformationDto>> getJobs() {
        return new ResponseEntity<>(personalReportJobService.getAllJobs().stream().map(job -> modelMapper.map(job, PersonalReportJobInformationDto.class)).toList(), OK);
    }


    @ApiOperation(value = "Get personal report job by id",
            notes = "Get personal report job by id",
            response = PersonalReportJobInformationDto.class,
            responseReference = "ResponseEntity<PersonalReportJobInformationDto>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @GetMapping("/{id}")
    public ResponseEntity<PersonalReportJobInformationDto> getJob(@PathVariable String id) {
        PersonalReportJobInformation jobInfo = personalReportJobService.getJob(id);
        return new ResponseEntity<>(modelMapper.map(jobInfo, PersonalReportJobInformationDto.class), OK);
    }

    @ApiOperation(value = "DELETE personal report job by id",
            notes = "DELETE personal report job by id",
            response = HttpStatus.class,
            responseReference = "ResponseEntity<HttpStatus>",
            httpMethod = DELETE,
            produces = APPLICATION_JSON_VALUE)
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteJob(@PathVariable String id) {
        personalReportJobService.deleteJob(id);
        return new ResponseEntity<>(NO_CONTENT);
    }

    @ApiOperation(value = "Stop personal report job by id",
            notes = "Stop personal report job by id",
            response = PersonalReportJobInformationDto.class,
            responseReference = "ResponseEntity<PersonalReportJobInformationDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping("/{id}/stop")
    public ResponseEntity<PersonalReportJobInformationDto> stopJob(@PathVariable String id) {
        PersonalReportJobInformation jobInfo = personalReportJobService.stopJob(id);
        return new ResponseEntity<>(modelMapper.map(jobInfo, PersonalReportJobInformationDto.class), OK);
    }

    @ApiOperation(value = "Resume personal report job by id",
            notes = "Resume personal report job by id",
            response = PersonalReportJobInformationDto.class,
            responseReference = "ResponseEntity<PersonalReportJobInformationDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping("/{id}/restart")
    public ResponseEntity<PersonalReportJobInformationDto> restartJob(@PathVariable String id) {
        PersonalReportJobInformation personalReportJobInformation = personalReportJobService.restartJob(id);
        return new ResponseEntity<>(modelMapper.map(personalReportJobInformation, PersonalReportJobInformationDto.class), OK);
    }
}
