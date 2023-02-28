package com.kanwise.user_service.controller.user;

import com.kanwise.user_service.error.handling.ExceptionHandling;
import com.kanwise.user_service.model.image.EditImageCommand;
import com.kanwise.user_service.model.image.Image;
import com.kanwise.user_service.model.image.dto.ImageDto;
import com.kanwise.user_service.model.image.request.ImageUploadRequest;
import com.kanwise.user_service.service.image.IUploadImageService;
import com.kanwise.user_service.service.user.IUserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RequestMapping("/user/{id}/image")
@RestController
public class ImageController extends ExceptionHandling {

    private final IUserService userService;
    private final IUploadImageService uploadImageService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Upload image",
            notes = "Upload user image",
            response = ImageDto.class,
            responseReference = "ResponseEntity<ImageDto>",
            httpMethod = "POST",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@authenticationFacade.isUserByIdAndHasAuthority(#id, 'USER_WRITE') or @authenticationFacade.admin")
    @PostMapping
    public ResponseEntity<ImageDto> uploadImage(@PathVariable("id") long id, @ModelAttribute @Valid ImageUploadRequest request) {
        Image image = uploadImageService.uploadImage(id, request);
        return new ResponseEntity<>(modelMapper.map(image, ImageDto.class), CREATED);
    }

    @ApiOperation(value = "Get images",
            notes = "Get user images",
            response = ImageDto.class,
            responseContainer = "Set",
            responseReference = "ResponseEntity<Set<ImageDto>",
            httpMethod = "GET",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@authenticationFacade.isUserByIdAndHasAuthority(#id, 'USER_READ') or @authenticationFacade.admin")
    @GetMapping
    public ResponseEntity<Set<ImageDto>> findImagesForUser(@PathVariable("id") long id) {
        Set<Image> images = userService.findImagesByUserId(id);
        return new ResponseEntity<>(images.stream().map(image -> modelMapper.map(image, ImageDto.class)).collect(toSet()), OK);
    }

    @ApiOperation(value = "Edit image",
            notes = "Edit image",
            response = ImageDto.class,
            responseReference = "ResponseEntity<ImageDto>",
            httpMethod = "PATCH",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@authenticationFacade.isUserByIdAndHasAuthority(#id, 'IMAGE_WRITE') or @authenticationFacade.admin")
    @PatchMapping("/{imageId}")
    public ResponseEntity<ImageDto> editImage(@PathVariable("id") long id, @PathVariable("imageId") long imageId, @RequestBody @Valid EditImageCommand command) {
        Image image = uploadImageService.editImagePartially(imageId, command);
        return new ResponseEntity<>(modelMapper.map(image, ImageDto.class), OK);
    }
}
