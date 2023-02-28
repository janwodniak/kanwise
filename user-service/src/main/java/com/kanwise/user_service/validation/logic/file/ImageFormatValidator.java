package com.kanwise.user_service.validation.logic.file;

import com.kanwise.user_service.validation.annotation.file.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.imaging.ImageFormats.valueOf;
import static org.apache.commons.io.FilenameUtils.getExtension;

@Service
@Scope("prototype")
public class ImageFormatValidator implements ConstraintValidator<ImageFormat, MultipartFile> {

    private ImageFormats[] formats;

    @Override
    public void initialize(ImageFormat constraintAnnotation) {
        this.formats = constraintAnnotation.formats();
    }


    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        return ofNullable(multipartFile)
                .map(file -> getExtension(file.getOriginalFilename()))
                .map(extension -> {
                    ImageFormats imageFormats;
                    try {
                        imageFormats = valueOf(extension.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                    return asList(formats).contains(imageFormats);
                })
                .orElse(true);
    }
}
