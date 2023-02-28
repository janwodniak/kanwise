package com.kanwise.kanwise_service.controller.join.request;

import com.kanwise.kanwise_service.error.handling.ExceptionHandling;
import com.kanwise.kanwise_service.model.join.request.JoinRequest;
import com.kanwise.kanwise_service.model.join.request.command.CreateJoinRequestCommand;
import com.kanwise.kanwise_service.model.join.request.dto.JoinRequestDto;
import com.kanwise.kanwise_service.service.join.request.IJoinRequestService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RequestMapping("/join/request")
@RestController
public class JoinRequestController extends ExceptionHandling {

    private final IJoinRequestService joinRequestService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Create join request",
            notes = "This endpoint is used to create join request.",
            response = JoinRequestDto.class,
            responseReference = "ResponseEntity<JoinRequestDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping
    public ResponseEntity<JoinRequestDto> createJoinRequest(@RequestBody @Valid CreateJoinRequestCommand command) {
        JoinRequest joinRequest = joinRequestService.saveJoinRequest(modelMapper.map(command, JoinRequest.class));
        return new ResponseEntity<>(modelMapper.map(joinRequest, JoinRequestDto.class), CREATED);
    }

    @ApiOperation(value = "Get join request by id",
            notes = "This endpoint is used to get join request by id.",
            response = JoinRequestDto.class,
            responseReference = "ResponseEntity<JoinRequestDto>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping("/{joinRequestId}")
    public ResponseEntity<JoinRequestDto> findJoinRequestById(@PathVariable("joinRequestId") long joinRequestId) {
        JoinRequest joinRequest = joinRequestService.findJoinRequestById(joinRequestId);
        return new ResponseEntity<>(modelMapper.map(joinRequest, JoinRequestDto.class), OK);
    }
}
