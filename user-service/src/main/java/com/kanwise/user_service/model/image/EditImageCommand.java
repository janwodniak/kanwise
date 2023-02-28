package com.kanwise.user_service.model.image;

import com.kanwise.user_service.validation.annotation.common.ValueOfEnum;
import lombok.Builder;

@Builder
public record EditImageCommand(
        @ValueOfEnum(enumClass = ImageRole.class, message = "INVALID_IMAGE_ROLE")
        String imageRole) {
}
