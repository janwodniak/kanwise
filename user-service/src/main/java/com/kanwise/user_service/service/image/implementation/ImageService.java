package com.kanwise.user_service.service.image.implementation;

import com.kanwise.user_service.model.image.Image;
import com.kanwise.user_service.repository.image.ImageRepository;
import com.kanwise.user_service.service.image.IImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImageService implements IImageService {

    private final ImageRepository imageRepository;

    @Transactional
    @Override
    public Image saveImage(Image image) {
        return imageRepository.save(image);
    }
}
