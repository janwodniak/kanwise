package com.kanwise.api_gateway.excpetion.handling;


import com.kanwise.api_gateway.excpetion.custom.GatewayAuthorizationException;
import com.kanwise.api_gateway.excpetion.custom.GatewayCommunicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.kanwise.api_gateway.model.http.response.HttpResponseAttributesKey.HTTP_STATUS;
import static com.kanwise.api_gateway.model.http.response.HttpResponseAttributesKey.HTTP_STATUS_CODE;
import static com.kanwise.api_gateway.model.http.response.HttpResponseAttributesKey.MESSAGE;
import static com.kanwise.api_gateway.model.http.response.HttpResponseAttributesKey.REASON;
import static com.kanwise.api_gateway.model.http.response.HttpResponseAttributesKey.TIMESTAMP;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Map.of;
import static org.springframework.core.annotation.MergedAnnotations.SearchStrategy.TYPE_HIERARCHY;
import static org.springframework.core.annotation.MergedAnnotations.from;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RequiredArgsConstructor
@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    private final Clock clock;

    private final List<ExceptionRule> exceptionsRules = List.of(
            new ExceptionRule(GatewayAuthorizationException.class, UNAUTHORIZED),
            new ExceptionRule(GatewayCommunicationException.class, INTERNAL_SERVER_ERROR)
    );

    private Map<String, Object> constructAttributesWithExceptionRule(Throwable error, ExceptionRule exceptionRule) {
        return of(HTTP_STATUS_CODE.getKey(), exceptionRule.status().value(),
                MESSAGE.getKey(), error.getMessage(),
                TIMESTAMP.getKey(), getTimestamp(),
                REASON.getKey(), exceptionRule.status().getReasonPhrase(),
                HTTP_STATUS.getKey(), exceptionRule.status());
    }

    private String getTimestamp() {
        return LocalDateTime.now(clock).format(ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = getError(request);
        return getExceptionRule(error)
                .map(exceptionRule -> constructAttributesWithExceptionRule(error, exceptionRule))
                .orElseGet(() -> constructAttributes(error));
    }

    private Optional<ExceptionRule> getExceptionRule(Throwable error) {
        return exceptionsRules.stream()
                .map(exceptionRule -> exceptionRule.exceptionClass().isInstance(error) ? exceptionRule : null)
                .filter(Objects::nonNull)
                .findFirst();
    }

    private Map<String, Object> constructAttributes(Throwable error) {
        return of(HTTP_STATUS_CODE.getKey(), determineHttpStatus(error).value(),
                MESSAGE.getKey(), error.getMessage(),
                TIMESTAMP.getKey(), getTimestamp(),
                REASON.getKey(), determineHttpStatus(error).getReasonPhrase(),
                HTTP_STATUS.getKey(), determineHttpStatus(error));
    }

    private HttpStatus determineHttpStatus(Throwable error) {
        return (error instanceof ResponseStatusException statusException) ? statusException.getStatus() : from(error.getClass(), TYPE_HIERARCHY)
                .get(ResponseStatus.class)
                .getValue(HTTP_STATUS_CODE.getKey(), HttpStatus.class)
                .orElse(INTERNAL_SERVER_ERROR);
    }
}
