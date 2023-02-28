package com.kanwise.api_gateway.excpetion.handling;

import com.kanwise.api_gateway.excpetion.custom.GatewayAuthorizationException;
import com.kanwise.api_gateway.excpetion.custom.GatewayCommunicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GlobalErrorAttributesTest {

    @Autowired
    GlobalErrorAttributes globalErrorAttributes;

    @DisplayName("Should get error attributes")
    @Nested
    class ShouldGetErrorAttributes {

        @Test
        void shouldGetErrorAttributesForGatewayCommunicationException() {
            // Given
            String message = "test message";
            MockServerRequest mockServerRequest = MockServerRequest.builder()
                    .attribute(DefaultErrorAttributes.class.getName() + ".ERROR", new GatewayCommunicationException(message))
                    .build();
            // When
            Map<String, Object> errorAttributes = globalErrorAttributes.getErrorAttributes(mockServerRequest, ErrorAttributeOptions.defaults());
            // Then
            assertEquals(message, errorAttributes.get("message"));
            assertTrue(errorAttributes.containsKey("timestamp"));
            assertEquals(INTERNAL_SERVER_ERROR.getReasonPhrase(), errorAttributes.get("reason"));
            assertEquals(INTERNAL_SERVER_ERROR.value(), errorAttributes.get("httpStatusCode"));
        }

        @Test
        void shouldGetErrorAttributesFromGatewayAuthorizationException() {
            // Given
            String message = "test message";
            MockServerRequest mockServerRequest = MockServerRequest.builder()
                    .attribute(DefaultErrorAttributes.class.getName() + ".ERROR", new GatewayAuthorizationException(message))
                    .build();
            // When
            Map<String, Object> errorAttributes = globalErrorAttributes.getErrorAttributes(mockServerRequest, ErrorAttributeOptions.defaults());
            // Then
            assertEquals(message, errorAttributes.get("message"));
            assertTrue(errorAttributes.containsKey("timestamp"));
            assertEquals(UNAUTHORIZED.getReasonPhrase(), errorAttributes.get("reason"));
            assertEquals(UNAUTHORIZED.value(), errorAttributes.get("httpStatusCode"));
        }

        @Test
        void shouldGetErrorAttributesFromResponseStatusException() {
            // Given
            String message = "test message";
            MockServerRequest mockServerRequest = MockServerRequest.builder()
                    .attribute(DefaultErrorAttributes.class.getName() + ".ERROR", new RuntimeException(message))
                    .build();
            // When
            Map<String, Object> errorAttributes = globalErrorAttributes.getErrorAttributes(mockServerRequest, ErrorAttributeOptions.defaults());
            // Then
            assertTrue(errorAttributes.containsKey("timestamp"));
            assertEquals(INTERNAL_SERVER_ERROR.getReasonPhrase(), errorAttributes.get("reason"));
            assertEquals(INTERNAL_SERVER_ERROR.value(), errorAttributes.get("httpStatusCode"));
        }

        @Test
        void shouldGetErrorAttributesByResponseStatusException() {
            // Given
            String message = "test message";
            MockServerRequest mockServerRequest = MockServerRequest.builder()
                    .attribute(DefaultErrorAttributes.class.getName() + ".ERROR", new ResponseStatusException(BAD_REQUEST, message))
                    .build();
            // When
            Map<String, Object> errorAttributes = globalErrorAttributes.getErrorAttributes(mockServerRequest, ErrorAttributeOptions.defaults());
            // Then
            assertTrue(errorAttributes.containsKey("timestamp"));
            assertEquals(BAD_REQUEST.getReasonPhrase(), errorAttributes.get("reason"));
            assertEquals(BAD_REQUEST.value(), errorAttributes.get("httpStatusCode"));
        }
    }
}