package com.kanwise.user_service.model.user.mapping;


import com.kanwise.clients.user_service.user.model.UserDataDto;
import com.kanwise.user_service.model.user.User;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserToUserDataDtoConverter implements Converter<User, UserDataDto> {


    @Override
    public UserDataDto convert(MappingContext<User, UserDataDto> mappingContext) {
        User source = mappingContext.getSource();
        return UserDataDto.builder()
                .data(constructData(source))
                .build();
    }

    private Map<String, Object> constructData(User source) {
        return Map.of(
                "username", source.getUsername(),
                "email", source.getEmail(),
                "firstName", source.getFirstName(),
                "lastName", source.getLastName()
        );
    }
}
