package com.kanwise.user_service.model.image.mapping;

import com.kanwise.user_service.controller.user.UserController;
import com.kanwise.user_service.model.image.Image;
import com.kanwise.user_service.model.image.dto.ImageDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class ImageToImageDtoConverter implements Converter<Image, ImageDto> {
    @Override
    public ImageDto convert(MappingContext<Image, ImageDto> mappingContext) {
        Image image = mappingContext.getSource();
        ImageDto imageDto = new ImageDto(image);
        imageDto.add(linkTo(methodOn(UserController.class).findUserById(image.getUser().getId())).withRel("user"));
        return imageDto;
    }
}
