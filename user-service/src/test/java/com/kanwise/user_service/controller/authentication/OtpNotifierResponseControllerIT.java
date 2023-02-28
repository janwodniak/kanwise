package com.kanwise.user_service.controller.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.user_service.error.custom.security.otp.OtpNotFoundException;
import com.kanwise.user_service.model.otp.OneTimePassword;
import com.kanwise.user_service.model.otp.OtpSmsNotifierResponse;
import com.kanwise.user_service.service.otp.IOtpService;
import com.kanwise.user_service.test.DatabaseCleaner;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.kanwise.clients.user_service.authentication.model.OtpStatus.CREATED;
import static com.kanwise.clients.user_service.authentication.model.OtpStatus.DELIVERED;
import static com.kanwise.clients.user_service.authentication.model.OtpStatus.FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-kafka-disabled")
@Testcontainers
class OtpNotifierResponseControllerIT {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DatabaseCleaner databaseCleaner;
    private final IOtpService otpService;

    @Autowired
    OtpNotifierResponseControllerIT(MockMvc mockMvc, ObjectMapper objectMapper, DatabaseCleaner databaseCleaner, IOtpService otpService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.databaseCleaner = databaseCleaner;
        this.otpService = otpService;
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        databaseCleaner.cleanUp();
    }

    @Nested
    class ShouldProcessSmsOtpResponse {
        @Test
        void shouldProcessSmsOtpResponse() throws Exception {
            // Given
            long otpId = 2L;
            OtpSmsNotifierResponse.builder()
                    .otpId(otpId)
                    .status(FAILED)
                    .build();
            // When
            OneTimePassword oneTimePasswordBeforeResponse = otpService.getOneTimePasswordById(otpId);
            assertNotNull(oneTimePasswordBeforeResponse);
            assertEquals(CREATED, oneTimePasswordBeforeResponse.getStatus());
            // Then
            mockMvc.perform(post("/auth/otp/sms/response")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(OtpSmsNotifierResponse.builder()
                                    .otpId(otpId)
                                    .status(DELIVERED)
                                    .build())))
                    .andExpect(status().isOk());

            OneTimePassword oneTimePassword = otpService.getOneTimePasswordById(otpId);
            assertNotNull(oneTimePassword);
            assertEquals(DELIVERED, oneTimePassword.getStatus());
        }
    }

    @Nested
    class ShouldNotProcessSmsOtpResponse {
        @Test
        void shouldNotProcessSmsOtpResponse() throws Exception {
            // Given
            long otpId = 3L;
            OtpSmsNotifierResponse.builder()
                    .otpId(otpId)
                    .status(FAILED)
                    .build();
            // When
            assertThrows(OtpNotFoundException.class, () -> otpService.getOneTimePasswordById(otpId));
            // Then
            mockMvc.perform(post("/auth/otp/sms/response")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(OtpSmsNotifierResponse.builder()
                                    .otpId(otpId)
                                    .status(FAILED)
                                    .build())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("OTP_WITH_ID_%s_NOT_FOUND".formatted(otpId)))
                    .andDo(print());

            assertThrows(OtpNotFoundException.class, () -> otpService.getOneTimePasswordById(otpId));
        }
    }
}