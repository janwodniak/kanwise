package com.kanwise.report_service.controller.subscriber;

import com.kanwise.report_service.error.handling.ExceptionHandling;
import com.kanwise.report_service.model.job_information.common.JobStatus;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.job_information.personal.dto.PersonalReportJobInformationDto;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.model.job_information.project.dto.ProjectReportJobInformationDto;
import com.kanwise.report_service.model.subscriber.Subscriber;
import com.kanwise.report_service.model.subscriber.command.CreateSubscriberCommand;
import com.kanwise.report_service.model.subscriber.command.EditSubscriberCommand;
import com.kanwise.report_service.model.subscriber.command.EditSubscriberPartiallyCommand;
import com.kanwise.report_service.model.subscriber.dto.SubscriberDto;
import com.kanwise.report_service.service.subscriber.common.ISubscriberService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.kanwise.report_service.model.http.HttpMethod.PATCH;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/subscriber")
public class SubscriberController extends ExceptionHandling {

    private final ISubscriberService subscriberService;

    private final ModelMapper modelMapper;

    @ApiOperation(value = "Create subscriber",
            notes = "Create subscriber",
            response = SubscriberDto.class,
            responseReference = "ResponseEntity<SubscriberDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping
    public ResponseEntity<SubscriberDto> createSubscriber(@Valid @RequestBody CreateSubscriberCommand createSubscriberCommand) {
        Subscriber subscriber = subscriberService.createSubscriber(modelMapper.map(createSubscriberCommand, Subscriber.class));
        return new ResponseEntity<>(modelMapper.map(subscriber, SubscriberDto.class), CREATED);
    }

    @ApiOperation(value = "Get subscriber",
            notes = "Get subscriber",
            response = SubscriberDto.class,
            responseReference = "ResponseEntity<SubscriberDto>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @GetMapping("/{username}")
    public ResponseEntity<SubscriberDto> getSubscriber(@PathVariable("username") String username) {
        Subscriber subscriber = subscriberService.getSubscriber(username);
        return new ResponseEntity<>(modelMapper.map(subscriber, SubscriberDto.class), OK);
    }

    @ApiOperation(value = "Get subscribers",
            notes = "Get subscribers",
            response = SubscriberDto.class,
            responseReference = "ResponseEntity<List<SubscriberDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @GetMapping
    public ResponseEntity<Set<SubscriberDto>> getSubscribers() {
        List<Subscriber> subscribers = subscriberService.getAllSubscribers();
        return new ResponseEntity<>(subscribers.stream().map(subscriber -> modelMapper.map(subscriber, SubscriberDto.class)).collect(toSet()), OK);
    }

    @ApiOperation(value = "Delete subscriber",
            notes = "Delete subscriber",
            response = SubscriberDto.class,
            responseReference = "ResponseEntity<SubscriberDto>",
            httpMethod = DELETE,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteSubscriber(@PathVariable("username") String username) {
        subscriberService.deleteSubscriber(username);
        return new ResponseEntity<>(NO_CONTENT);
    }

    @ApiOperation(value = "Edit subscriber",
            notes = "Edit subscriber",
            response = SubscriberDto.class,
            responseReference = "ResponseEntity<SubscriberDto>",
            httpMethod = PUT,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PutMapping("/{username}")
    public ResponseEntity<SubscriberDto> editSubscriber(@PathVariable("username") String username, @Valid @RequestBody EditSubscriberCommand command) {
        Subscriber subscriber = subscriberService.editSubscriber(username, command);
        return new ResponseEntity<>(modelMapper.map(subscriber, SubscriberDto.class), OK);
    }

    @ApiOperation(value = "Edit subscriber partially",
            notes = "Edit subscriber partially",
            response = SubscriberDto.class,
            responseReference = "ResponseEntity<SubscriberDto>",
            httpMethod = PATCH,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PatchMapping("/{username}")
    public ResponseEntity<SubscriberDto> editSubscriberPartially(@PathVariable("username") String username, @Valid @RequestBody EditSubscriberPartiallyCommand command) {
        Subscriber subscriber = subscriberService.editSubscriberPartially(username, command);
        return new ResponseEntity<>(modelMapper.map(subscriber, SubscriberDto.class), OK);
    }


    @ApiOperation(value = "Get subscriber personal report",
            notes = "Get subscriber personal report",
            response = PersonalReportJobInformationDto.class,
            responseReference = "ResponseEntity<PersonalReportJobInformationDto>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @GetMapping("/{username}/reports/personal")
    public ResponseEntity<Set<PersonalReportJobInformationDto>> getPersonalReports(@PathVariable("username") String username, @RequestParam("status") Optional<JobStatus> status) {
        Set<PersonalReportJobInformation> jobs = status
                .map(jobStatus -> subscriberService.getPersonalReports(username, jobStatus))
                .orElseGet(() -> subscriberService.getPersonalReports(username));
        return new ResponseEntity<>(jobs.stream().map(jobInformation -> modelMapper.map(jobInformation, PersonalReportJobInformationDto.class)).collect(toSet()), OK);
    }


    @ApiOperation(value = "Get subscriber project report",
            notes = "Get subscriber project report",
            response = ProjectReportJobInformationDto.class,
            responseReference = "ResponseEntity<ProjectReportJobInformationDto>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @GetMapping("/{username}/reports/project")
    public ResponseEntity<Set<ProjectReportJobInformationDto>> getProjectReports(@PathVariable("username") String username, @RequestParam("status") Optional<JobStatus> status) {
        Set<ProjectReportJobInformation> jobs = status
                .map(jobStatus -> subscriberService.getProjectReports(username, jobStatus))
                .orElseGet(() -> subscriberService.getProjectReports(username));
        return new ResponseEntity<>(jobs.stream().map(jobInformation -> modelMapper.map(jobInformation, ProjectReportJobInformationDto.class)).collect(toSet()), OK);
    }
}
