package com.kanwise.report_service.model.file;

public enum FileUploadStatus {
    SUCCESS,
    FAILED,
    IN_PROGRESS;

    public boolean isSuccessful() {
        return this == SUCCESS;
    }
}
