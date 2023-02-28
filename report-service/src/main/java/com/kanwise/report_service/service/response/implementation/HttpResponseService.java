package com.kanwise.report_service.service.response.implementation;


import com.kanwise.report_service.model.response.HttpResponse;
import com.kanwise.report_service.service.response.common.IHttpResponseService;
import com.kanwise.report_service.service.response.common.IResponseMessageFormatter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;

import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Service
public class HttpResponseService implements IHttpResponseService, IResponseMessageFormatter {

    private final Clock clock;

    @Override
    public HttpResponse generateHttpResponse(@NonNull HttpStatus httpStatus, @NonNull String message) {
        return HttpResponse.builder()
                .timestamp(now(clock))
                .httpStatusCode(httpStatus.value())
                .httpStatus(httpStatus)
                .reason(httpStatus.getReasonPhrase().toUpperCase())
                .message(replace(message.toUpperCase(), "\\s+", "_"))
                .build();
    }
}
