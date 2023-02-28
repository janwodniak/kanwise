package com.kanwise.report_service.service.response.implementation;

import com.kanwise.report_service.model.response.HttpResponse;
import com.kanwise.report_service.service.response.common.IHttpResponseService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.now;
import static java.time.ZonedDateTime.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
class HttpResponseServiceTest {

    private static final ZonedDateTime NOW = of(
            2022, 12, 21, 14, 0, 0, 0, ZoneId.of("UTC")
    );
    private IHttpResponseService httpResponseService;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        httpResponseService = new HttpResponseService(clock);
    }

    @Test
    void shouldGenerateHttpResponse() {
        // Given
        // When
        when(clock.instant()).thenReturn(NOW.toInstant());
        when(clock.getZone()).thenReturn(NOW.getZone());
        HttpResponse httpResponse = httpResponseService.generateHttpResponse(OK, "test");
        // Then
        assertNotNull(httpResponse);
        assertEquals(OK, httpResponse.httpStatus());
        assertEquals(OK.value(), httpResponse.httpStatusCode());
        assertEquals(OK.getReasonPhrase().toUpperCase(), httpResponse.reason());
        assertEquals("TEST", httpResponse.message());
        assertNotNull(httpResponse.timestamp());
        Assertions.assertEquals(httpResponse.timestamp(), now(clock));
    }

    @Test
    void shouldNotGenerateHttpResponseWithNullHttpStatus() {
        // Given
        // When
        // Then
        assertThrows(NullPointerException.class, () -> httpResponseService.generateHttpResponse(null, "test"));
    }

    @Test
    void shouldNotGenerateHttpResponseWithNullMessage() {
        // Given
        // When
        // Then
        assertThrows(NullPointerException.class, () -> httpResponseService.generateHttpResponse(OK, null));
    }
}