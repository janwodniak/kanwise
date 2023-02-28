package com.kanwise.user_service.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.clients.user_service.user.model.UserDataDto;
import com.kanwise.user_service.test.DatabaseCleaner;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-kafka-disabled")
@Testcontainers
class UserDataControllerIT {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DatabaseCleaner databaseCleaner;

    @Autowired
    UserDataControllerIT(MockMvc mockMvc, ObjectMapper objectMapper, DatabaseCleaner databaseCleaner) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.databaseCleaner = databaseCleaner;
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        databaseCleaner.cleanUp();
    }

    @Nested
    class ShouldRetrieveUserData {

        @Test
        void shouldRetrieveUserData() throws Exception {
            // Given
            String username = "jargrave0";
            // When
            // Then
            String responseJson = mockMvc.perform(get("/user/%s/data".formatted(username))
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            UserDataDto userDataDto = objectMapper.readValue(responseJson, UserDataDto.class);
            Map<String, Object> data = userDataDto.data();
            Assertions.assertNotNull(data);
            Assertions.assertEquals(username, data.get("username"));
            Assertions.assertEquals("jolettatiger.kanwise@gmail.com", data.get("email"));
            Assertions.assertEquals("Joletta", data.get("firstName"));
            Assertions.assertEquals("Tiger", data.get("lastName"));
        }
    }

    @Nested
    class ShouldNotRetrieveUserData {

        @Test
        void shouldNotRetrieveUserDataIfUserDoesNotExist() throws Exception {
            // Given
            String username = "nonExistingUsername";
            // When
            // Then
            mockMvc.perform(get("/user/%s/data".formatted(username))
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"));
        }
    }
}