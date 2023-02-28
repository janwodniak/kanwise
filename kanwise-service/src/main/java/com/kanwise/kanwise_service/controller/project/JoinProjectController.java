package com.kanwise.kanwise_service.controller.project;

import com.kanwise.kanwise_service.error.handling.ExceptionHandling;
import com.kanwise.kanwise_service.model.join.request.dto.JoinRequestDto;
import com.kanwise.kanwise_service.model.join.response.dto.JoinResponseDto;
import com.kanwise.kanwise_service.service.join.request.IJoinRequestService;
import com.kanwise.kanwise_service.service.join.response.IJoinResponseService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RequestMapping("/project")
@RestController
public class JoinProjectController extends ExceptionHandling {

    private final IJoinRequestService joinRequestService;
    private final IJoinResponseService joinResponseService;
    private final ModelMapper modelMapper;


    @ApiOperation(value = "Get join requests for the project",
            notes = "This endpoint is used to get join requests by project projectId.",
            response = JoinRequestDto.class,
            responseReference = "ResponseEntity<Set<JoinRequestDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping("/{projectId}/join/requests")
    public ResponseEntity<Set<JoinRequestDto>> getJoinRequests(@PathVariable("projectId") long projectId, @RequestParam("verified") Optional<Boolean> verified) {
        return new ResponseEntity<>(joinRequestService.findJoinRequestsForProject(projectId, verified.orElse(false)).stream()
                .map(joinRequest -> modelMapper.map(joinRequest, JoinRequestDto.class)).collect(toSet()), OK);
    }

    @ApiOperation(value = "Get join responses for the project",
            notes = "This endpoint is used to get join responses by project projectId.",
            response = JoinResponseDto.class,
            responseReference = "ResponseEntity<Set<JoinResponseDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping("/{projectId}/join/responses")
    public ResponseEntity<Set<JoinResponseDto>> getJoinResponses(@PathVariable("projectId") long projectId) {
        return new ResponseEntity<>(joinResponseService.findJoinResponsesForProject(projectId).stream()
                .map(joinResponse -> modelMapper.map(joinResponse, JoinResponseDto.class)).collect(toSet()), OK);
    }
}
