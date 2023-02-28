package com.kanwise.report_service.service.spaces.implementation;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.kanwise.report_service.model.file.FileUploadStatus;
import com.kanwise.report_service.service.spaces.common.ISpacesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static com.amazonaws.services.s3.model.CannedAccessControlList.PublicRead;
import static com.cronutils.utils.StringUtils.EMPTY;
import static com.kanwise.report_service.model.file.FileUploadStatus.FAILED;
import static com.kanwise.report_service.model.file.FileUploadStatus.SUCCESS;
import static java.util.concurrent.CompletableFuture.completedFuture;


@Slf4j
@Service
@RequiredArgsConstructor
public class SpacesService implements ISpacesService {

    private final AmazonS3 space;

    @Async
    public CompletableFuture<FileUploadStatus> uploadFile(MultipartFile file, String path, String spaceName) {
        try {
            PutObjectRequest request = new PutObjectRequest(
                    spaceName,
                    path + file.getOriginalFilename(),
                    file.getInputStream(),
                    getFileMetadata(file)
            ).withCannedAcl(PublicRead);
            space.putObject(request);
            return completedFuture(SUCCESS);
        } catch (IOException | AmazonS3Exception e) {
            log.error("Failed to upload file to space", e);
            return completedFuture(FAILED);
        }
    }

    private ObjectMetadata getFileMetadata(MultipartFile file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());
        return objectMetadata;
    }

    @Override
    public URL checkIfDirectoryExists(String spaceName, String directoryName, boolean createIfNotExists) {
        boolean objectExist = space.doesObjectExist(spaceName, directoryName);

        if (!objectExist && createIfNotExists) {
            PutObjectRequest request = new PutObjectRequest(spaceName, directoryName, EMPTY);
            space.putObject(request);
        }

        return space.getUrl(spaceName, directoryName);
    }
}
