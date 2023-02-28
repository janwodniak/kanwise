package com.kanwise.user_service.error.custom.user;

public class ImageNotFoundException extends RuntimeException {
    public ImageNotFoundException() {
        super("IMAGE_NOT_FOUND");
    }
}
