package com.kanwise.api_gateway.controller;

import com.kanwise.api_gateway.model.http.response.HttpResponse;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Clock;

import static com.kanwise.api_gateway.model.http.response.ErrorMessage.CONNECTION_ERROR_TRY_AGAIN_LATER;
import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RequiredArgsConstructor
@RestController
public class FallbackController {

    private final Clock clock;

    @ApiOperation(
            value = "Fallback method",
            notes = "This endpoint is used to return a fallback response when the primary service is unavailable or encounters an error.",
            response = HttpResponse.class,
            responseReference = "JSON object with fields for timestamp, error code, and error message",
            httpMethod = "GET, POST, PUT, PATCH, DELETE",
            produces = APPLICATION_JSON_VALUE)
    @RequestMapping(path = "/fallback", method = {GET, POST, PUT, PATCH, DELETE})
    public Mono<ResponseEntity<HttpResponse>> fallback() {
        return Mono.just(ResponseEntity.status(INTERNAL_SERVER_ERROR).body(generateFallbackResponse()));
    }

    private HttpResponse generateFallbackResponse() {
        return HttpResponse.builder()
                .timestamp(now(clock))
                .httpStatusCode(INTERNAL_SERVER_ERROR.value())
                .httpStatus(INTERNAL_SERVER_ERROR)
                .reason(INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(CONNECTION_ERROR_TRY_AGAIN_LATER)
                .build();
    }
}
