package com.kanwise.user_service.model.image.request;

import com.kanwise.user_service.validation.annotation.file.FileSize;
import com.kanwise.user_service.validation.annotation.file.ImageFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

import static org.apache.commons.imaging.ImageFormats.JPEG;
import static org.apache.commons.imaging.ImageFormats.PNG;

@Getter
@Setter
@Builder
public class ImageUploadRequest {
    @FileSize(maxSizeInMB = 5, message = "MAX_FILE_IS_5MB")
    @ImageFormat(formats = {JPEG, PNG})
    @NotNull(message = "FILE_NOT_NULL")
    private MultipartFile file;
}