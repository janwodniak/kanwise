package com.kanwise.report_service.service.spaces.implementation;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.kanwise.report_service.service.spaces.common.ISpacesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockMultipartFile;

import static com.kanwise.report_service.model.file.FileUploadStatus.FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(classes = {SpacesService.class})
class SpacesServiceTest {

    private final ISpacesService spacesService;

    @MockBean
    private AmazonS3 space;

    @Autowired
    public SpacesServiceTest(SpacesService spacesService) {
        this.spacesService = spacesService;
    }

    @Test
    void shouldHandleUploadFailure(CapturedOutput output) {
        // Given
        String expectedMessage = "Failed to upload file to space";
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Spring Framework".getBytes());
        // When
        when(space.putObject(any(PutObjectRequest.class))).thenThrow(AmazonS3Exception.class);
        // Then
        spacesService.uploadFile(file, "test", "test").thenAccept(fileUploadStatus -> assertEquals(FAILED, fileUploadStatus));
        assertTrue(output.toString().contains(expectedMessage));
    }
}