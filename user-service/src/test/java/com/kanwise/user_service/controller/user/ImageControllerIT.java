package com.kanwise.user_service.controller.user;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.user_service.model.authentication.request.LoginRequest;
import com.kanwise.user_service.model.image.EditImageCommand;
import com.kanwise.user_service.model.image.request.ImageUploadRequest;
import com.kanwise.user_service.test.DatabaseCleaner;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

import static com.kanwise.user_service.model.image.ImageRole.PROFILE_IMAGE;
import static com.kanwise.user_service.model.image.ImageRole.UNSIGNED_IMAGE;
import static java.lang.String.format;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@SpringBootTest(value = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class ImageControllerIT {

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.13.0"))
            .withServices(S3);
    private final AmazonS3 amazonS3;
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DatabaseCleaner databaseCleaner;

    @Autowired
    public ImageControllerIT(AmazonS3 amazonS3, MockMvc mockMvc, ObjectMapper objectMapper, DatabaseCleaner databaseCleaner) {
        this.amazonS3 = amazonS3;
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.databaseCleaner = databaseCleaner;
    }

    @DynamicPropertySource
    static void overrideConfiguration(DynamicPropertyRegistry registry) {
        registry.add("digitalocean.spaces.accessKey", localStack::getAccessKey);
        registry.add("digitalocean.spaces.secretKey", localStack::getSecretKey);
        registry.add("digitalocean.spaces.signing-region", localStack::getRegion);
    }

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        localStack.execInContainer("awslocal", "s3", "mb", "s3://kanwise");
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        databaseCleaner.cleanUp();
    }

    private String getAdminAuthorizationHeader() throws Exception {
        return mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("celders1", "Password123*"))))
                .andExpect(header().exists(AUTHORIZATION))
                .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")))
                .andReturn()
                .getResponse()
                .getHeader(AUTHORIZATION);
    }

    @TestConfiguration
    static class AwsApiConfig {

        @Primary
        @Bean
        public AmazonS3 amazonS3() {
            return AmazonS3ClientBuilder
                    .standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localStack.getEndpointOverride(S3).toString(), localStack.getRegion()))
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(localStack.getAccessKey(), localStack.getSecretKey())))
                    .build();
        }
    }

    @Nested
    class ShouldUploadImage {
        @Test
        void shouldUploadImage() throws Exception {
            // Given
            long userId = 1L;
            String username = "jargrave0";
            String filename = "test.jpeg";
            MockMultipartFile testFile = new MockMultipartFile("file", filename, "image/jpeg", new byte[2 * 1024 * 1024]);
            ImageUploadRequest imageUploadRequest = ImageUploadRequest.builder()
                    .file(testFile)
                    .build();
            // When
            mockMvc.perform(get("/user/" + userId)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));
            // Then
            mockMvc.perform(post("/user/" + userId + "/image")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader())
                            .flashAttr("imageUploadRequest", imageUploadRequest))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.uploadedAt").exists())
                    .andExpect(jsonPath("$.imageUrl").value(localStack.getEndpointOverride(S3).toString() + "/kanwise/images/%s/%s".formatted(username, filename)))
                    .andExpect(jsonPath("$.imageName").value(filename))
                    .andExpect(jsonPath("$._links.user.href").value(format("http://localhost/user/%s", userId)))
                    .andDo(print());

            Awaitility.await().until(() -> amazonS3.doesObjectExist("kanwise", "images/%s/%s".formatted(username, filename)));
        }
    }

    @Nested
    class ShouldNotUploadImage {
        @Test
        void shouldNotUploadImageWithInvalidFileSize() throws Exception {
            // Given
            long userId = 1L;
            String filename = "test.jpeg";
            int invalidFileSize = 6 * 1024 * 1024;
            MockMultipartFile testFile = new MockMultipartFile("file", filename, "image/jpeg", new byte[invalidFileSize]);
            ImageUploadRequest imageUploadRequest = ImageUploadRequest.builder()
                    .file(testFile)
                    .build();
            // When
            // Then
            mockMvc.perform(post("/user/" + userId + "/image")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader())
                            .flashAttr("imageUploadRequest", imageUploadRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'file' && @.message == 'MAX_FILE_IS_5MB')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotUploadImageWithInvalidFileType() throws Exception {
            // Given
            long userId = 1L;
            String filename = "test.txt";
            MockMultipartFile testFile = new MockMultipartFile("file", filename, "text/plain", new byte[2 * 1024 * 1024]);
            ImageUploadRequest imageUploadRequest = ImageUploadRequest.builder()
                    .file(testFile)
                    .build();
            // When
            // Then
            mockMvc.perform(post("/user/" + userId + "/image")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader())
                            .flashAttr("imageUploadRequest", imageUploadRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'file' && @.message == 'INVALID_IMAGE_FORMAT')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotUploadImageIfNotAuthorized() throws Exception {
            // Given
            long userId = 1L;
            String filename = "test.jpeg";
            MockMultipartFile testFile = new MockMultipartFile("file", filename, "image/jpeg", new byte[2 * 1024 * 1024]);
            ImageUploadRequest imageUploadRequest = ImageUploadRequest.builder()
                    .file(testFile)
                    .build();
            // When
            // Then
            mockMvc.perform(post("/user/" + userId + "/image")
                            .flashAttr("imageUploadRequest", imageUploadRequest))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("FULL_AUTHENTICATION_IS_REQUIRED_TO_ACCESS_THIS_RESOURCE"));
        }
    }

    @Nested
    class ShouldFindImage {

        @Test
        void shouldFindImagesForUser() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user/2/image")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(1, 2)))
                    .andExpect(jsonPath("$.[*].userId").value(containsInAnyOrder(2, 2)))
                    .andExpect(jsonPath("$.[*].uploadedAt").exists())
                    .andExpect(jsonPath("$.[*].imageUrl").value(containsInAnyOrder(
                            "https://fra1.digitaloceanspaces.com/kanwise/images/celders1/image1.jpeg",
                            "https://fra1.digitaloceanspaces.com/kanwise/images/celders1/image2.png")))
                    .andExpect(jsonPath("$.[*].imageName").value(containsInAnyOrder("image1.jpeg", "image2.png")))
                    .andExpect(jsonPath("$.[*].imageRole").value(containsInAnyOrder(UNSIGNED_IMAGE.name(), PROFILE_IMAGE.name())))
                    .andExpect(jsonPath("$.[*].links[0].href").value(containsInAnyOrder("http://localhost/user/2", "http://localhost/user/2")))
                    .andExpect(jsonPath("$.[*].links[0].rel").value(containsInAnyOrder("user", "user")))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotFindImage {

        @Test
        void shouldNotFindImagesForUserIfUserDoesNotExists() throws Exception {
            // Given
            // When
            mockMvc.perform(get("/user/51")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"));
            // Then
            mockMvc.perform(get("/user/51/image")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotFindImagesForUserIfNotAuthorized() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user/2/image"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("FULL_AUTHENTICATION_IS_REQUIRED_TO_ACCESS_THIS_RESOURCE"));
        }
    }

    @Nested
    class ShouldEditImage {

    }

    @Nested
    class ShouldNotEditImage {

        @Test
        void shouldNotEditImageWithInvalidImageRole() throws Exception {
            // Given
            long userId = 1L;
            long imageId = 1L;
            EditImageCommand editImageCommand = EditImageCommand.builder()
                    .imageRole("invalidImageRole")
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/user/" + userId + "/image/" + imageId)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader())
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editImageCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'imageRole' && @.message == 'INVALID_IMAGE_ROLE')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditImageIfImageDoesntExist() throws Exception {
            // Given
            EditImageCommand editImageCommand = EditImageCommand.builder()
                    .imageRole(PROFILE_IMAGE.name())
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/user/2/image/3")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader())
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editImageCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("IMAGE_NOT_FOUND"));
        }

        @Test
        void shouldNotEditImageIfUserIsNotAuthorized() throws Exception {
            // Given
            EditImageCommand editImageCommand = EditImageCommand.builder()
                    .imageRole(PROFILE_IMAGE.name())
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/user/2/image/1")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editImageCommand)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("FULL_AUTHENTICATION_IS_REQUIRED_TO_ACCESS_THIS_RESOURCE"));
        }
    }
}