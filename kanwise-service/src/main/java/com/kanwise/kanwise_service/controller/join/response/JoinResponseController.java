package com.kanwise.kanwise_service.controller.join.response;

import com.kanwise.kanwise_service.error.handling.ExceptionHandling;
import com.kanwise.kanwise_service.model.join.response.JoinResponse;
import com.kanwise.kanwise_service.model.join.response.command.CreateJoinResponseCommand;
import com.kanwise.kanwise_service.model.join.response.dto.JoinResponseDto;
import com.kanwise.kanwise_service.service.join.response.IJoinResponseService;
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
@RequestMapping("/join/response")
@RestController
public class JoinResponseController extends ExceptionHandling {

    private final IJoinResponseService joinResponseService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Create join response",
            notes = "This endpoint is used to create join response.",
            response = JoinResponseDto.class,
            responseReference = "ResponseEntity<JoinResponseDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping
    public ResponseEntity<JoinResponseDto> createJoinResponse(@RequestBody @Valid CreateJoinResponseCommand command) {
        JoinResponse joinResponse = joinResponseService.saveJoinResponse(modelMapper.map(command, JoinResponse.class));
        return new ResponseEntity<>(modelMapper.map(joinResponse, JoinResponseDto.class), CREATED);
    }

    @ApiOperation(value = "Get join response by id",
            notes = "This endpoint is used to get join response by id.",
            response = JoinResponseDto.class,
            responseReference = "ResponseEntity<JoinResponseDto>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping("/{joinResponseId}")
    public ResponseEntity<JoinResponseDto> findJoinResponseById(@PathVariable("joinResponseId") long joinResponseId) {
        JoinResponse joinResponse = joinResponseService.findJoinResponseById(joinResponseId);
        return new ResponseEntity<>(modelMapper.map(joinResponse, JoinResponseDto.class), OK);
    }
}
