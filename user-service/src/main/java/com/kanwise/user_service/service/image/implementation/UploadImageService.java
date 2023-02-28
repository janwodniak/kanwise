package com.kanwise.user_service.service.image.implementation;

import com.kanwise.user_service.configuration.spaces.SpacesNamesConfigurationProperties;
import com.kanwise.user_service.error.custom.user.ImageNotFoundException;
import com.kanwise.user_service.model.image.EditImageCommand;
import com.kanwise.user_service.model.image.Image;
import com.kanwise.user_service.model.image.request.ImageUploadRequest;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.repository.image.ImageRepository;
import com.kanwise.user_service.service.image.IImageService;
import com.kanwise.user_service.service.image.IUploadImageService;
import com.kanwise.user_service.service.spaces.ISpacesService;
import com.kanwise.user_service.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.Clock;

import static com.kanwise.user_service.model.file.FileUploadStatus.IN_PROGRESS;
import static com.kanwise.user_service.model.image.ImageRole.PROFILE_IMAGE;
import static com.kanwise.user_service.model.image.ImageRole.valueOf;
import static java.time.LocalDateTime.now;
import static java.util.Optional.ofNullable;

@Transactional
@RequiredArgsConstructor
@Service
public class UploadImageService implements IUploadImageService {


    private final ISpacesService spacesService;
    private final SpacesNamesConfigurationProperties spacesNamesConfigurationProperties;
    private final ImageRepository imageRepository;
    private final IImageService imageService;
    private final IUserService userService;
    private final Clock clock;


    @Override
    public Image uploadImage(long userId, ImageUploadRequest request) {
        String spaceName = spacesNamesConfigurationProperties.profileImages();


        MultipartFile file = request.getFile();

        User userById = userService.findUserById(userId);


        URL url = spacesService.checkIfDirectoryExists(spaceName, "images/%s/".formatted(userById.getUsername()), true);


        Image image = imageService.saveImage(Image.builder()
                .user(userById)
                .imageName(file.getOriginalFilename())
                .imageUrl(url.toString() + file.getOriginalFilename())
                .uploadedAt(now(clock))
                .uploadStatus(IN_PROGRESS)
                .imageRole(PROFILE_IMAGE)
                .build());

        spacesService.uploadFile(file, "images/%s/".formatted(userById.getUsername()), spaceName).thenAccept(fileUploadStatus -> {
            image.setUploadStatus(fileUploadStatus);
            imageService.saveImage(image);
        });
        return image;
    }

    @Override
    public Image editImagePartially(long id, EditImageCommand command) {
        return imageRepository.findById(id).map(image -> {
            ofNullable(command.imageRole()).ifPresent(imageRole -> {
                        if (valueOf(imageRole.toUpperCase()).equals(PROFILE_IMAGE)) {
                            image.getUser().setProfileImage(image);
                        }
                    }
            );
            return image;
        }).orElseThrow(ImageNotFoundException::new);
    }
}
