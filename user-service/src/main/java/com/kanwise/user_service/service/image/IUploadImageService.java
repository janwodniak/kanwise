package com.kanwise.user_service.service.image;

import com.kanwise.user_service.model.image.EditImageCommand;
import com.kanwise.user_service.model.image.Image;
import com.kanwise.user_service.model.image.request.ImageUploadRequest;

public interface IUploadImageService {
    Image uploadImage(long userId, ImageUploadRequest request);

    Image editImagePartially(long id, EditImageCommand command);
}
