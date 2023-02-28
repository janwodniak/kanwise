package com.kanwise.user_service.service.spaces;

import com.kanwise.user_service.model.file.FileUploadStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

public interface ISpacesService {

    @Async
    CompletableFuture<FileUploadStatus> uploadFile(MultipartFile file, String path, String spaceName);

    URL checkIfDirectoryExists(String spaceName, String directoryName, boolean createIfNotExists);
}
