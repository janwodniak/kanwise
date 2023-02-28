package com.kanwise.report_service.service.spaces.common;


import com.kanwise.report_service.model.file.FileUploadStatus;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

public interface ISpacesService {

    CompletableFuture<FileUploadStatus> uploadFile(MultipartFile file, String path, String spaceName);

    URL checkIfDirectoryExists(String spaceName, String directoryName, boolean createIfNotExists);
}
