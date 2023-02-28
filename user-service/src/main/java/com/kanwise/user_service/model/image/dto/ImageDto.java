package com.kanwise.user_service.model.image.dto;

import com.kanwise.user_service.model.image.Image;
import com.kanwise.user_service.model.image.ImageRole;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Value
@EqualsAndHashCode(callSuper = true)
public class ImageDto extends RepresentationModel<ImageDto> {
    long id;
    long userId;
    LocalDateTime uploadedAt;
    String imageUrl;
    String imageName;
    ImageRole imageRole;

    public ImageDto(Image image) {
        this.id = image.getId();
        this.userId = image.getUser().getId();
        this.uploadedAt = image.getUploadedAt();
        this.imageUrl = image.getImageUrl();
        this.imageName = image.getImageName();
        this.imageRole = image.getImageRole();
    }
}
