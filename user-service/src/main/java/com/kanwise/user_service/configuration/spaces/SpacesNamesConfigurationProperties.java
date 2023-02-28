package com.kanwise.user_service.configuration.spaces;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;


@Validated
@ConfigurationProperties("kanwise.digitalocean.spaces.names")
public record SpacesNamesConfigurationProperties(
        @NotEmpty(message = "PROFILE_IMAGES_SPACE_NAME_NOT_EMPTY") String profileImages
) {
}
