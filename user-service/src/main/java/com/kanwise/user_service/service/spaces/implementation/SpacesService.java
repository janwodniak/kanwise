package com.kanwise.user_service.service.spaces.implementation;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.kanwise.user_service.model.file.FileUploadStatus;
import com.kanwise.user_service.service.spaces.ISpacesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static com.amazonaws.services.s3.model.CannedAccessControlList.PublicRead;
import static com.kanwise.user_service.model.file.FileUploadStatus.FAILED;
import static com.kanwise.user_service.model.file.FileUploadStatus.SUCCESS;

@Service
@RequiredArgsConstructor
public class SpacesService implements ISpacesService {

    private final AmazonS3 space;

    public CompletableFuture<FileUploadStatus> uploadFile(MultipartFile file, String path, String spaceName) {
        try {
            space.putObject(new PutObjectRequest(
                    spaceName,
                    path + file.getOriginalFilename(),
                    file.getInputStream(),
                    getFileMetadata(file)
            ).withCannedAcl(PublicRead));
        } catch (IOException | AmazonS3Exception e) {
            return CompletableFuture.completedFuture(FAILED);
        }
        return CompletableFuture.completedFuture(SUCCESS);
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
            space.putObject(spaceName, directoryName, "");
        }

        return space.getUrl(spaceName, directoryName);
    }
}
