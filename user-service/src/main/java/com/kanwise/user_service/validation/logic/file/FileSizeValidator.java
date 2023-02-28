package com.kanwise.user_service.validation.logic.file;

import com.kanwise.user_service.validation.annotation.file.FileSize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
@Scope("prototype")
public class FileSizeValidator implements ConstraintValidator<FileSize, MultipartFile> {

    private static final Integer MB = 1024 * 1024;
    private long maxSizeInMB;

    @Override
    public void initialize(FileSize constraintAnnotation) {
        this.maxSizeInMB = constraintAnnotation.maxSizeInMB();
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        return ofNullable(multipartFile)
                .map(MultipartFile::getSize)
                .map(size -> size <= maxSizeInMB * MB)
                .orElse(true);
    }
}
