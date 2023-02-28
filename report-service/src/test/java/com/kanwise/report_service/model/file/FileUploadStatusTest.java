package com.kanwise.report_service.model.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUploadStatusTest {

    @Test
    void shouldReturnTrueWhenFileUploadStatusIsSuccessful() {
        // Given
        FileUploadStatus fileUploadStatus = FileUploadStatus.SUCCESS;
        // When
        boolean isSuccessful = fileUploadStatus.isSuccessful();
        // Then
        assertTrue(isSuccessful);
    }

    @ParameterizedTest
    @EnumSource(value = FileUploadStatus.class, names = {"FAILED", "IN_PROGRESS"})
    void shouldReturnFalseWhenFileUploadStatusIsNotSuccessful(FileUploadStatus fileUploadStatus) {
        // Given
        // When
        boolean isSuccessful = fileUploadStatus.isSuccessful();
        // Then
        assertFalse(isSuccessful);
    }
}