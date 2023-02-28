package com.kanwise.kanwise_service.service.response.implementation;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.kanwise_service.model.response.HttpResponse;
import com.kanwise.kanwise_service.service.response.IHttpResponseService;
import com.kanwise.kanwise_service.service.response.IResponseMessageFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;

import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Service
public class HttpResponseService implements IHttpResponseService, IResponseMessageFormatter {

    private final Clock clock;

    private final ObjectMapper objectMapper;

    @Override
    public HttpResponse generateHttpResponse(HttpStatus httpStatus, String message) {
        return HttpResponse.builder()
                .timestamp(now(clock))
                .httpStatusCode(httpStatus.value())
                .httpStatus(httpStatus)
                .reason(httpStatus.getReasonPhrase().toUpperCase())
                .message(replace(message.toUpperCase(), "\\s+", "_"))
                .build();
    }
}
