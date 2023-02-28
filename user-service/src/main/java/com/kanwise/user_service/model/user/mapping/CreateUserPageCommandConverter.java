package com.kanwise.user_service.model.user.mapping;

import com.kanwise.user_service.model.user.command.CreateUserPageCommand;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static org.springframework.data.domain.Sort.Direction;
import static org.springframework.data.domain.Sort.by;

@Service
public class CreateUserPageCommandConverter implements Converter<CreateUserPageCommand, Pageable> {
    @Override
    public Pageable convert(MappingContext<CreateUserPageCommand, Pageable> mappingContext) {
        CreateUserPageCommand userPage = mappingContext.getSource();
        return PageRequest.of(userPage.getPageNumber(),
                userPage.getPageSize(),
                by(Direction.valueOf(userPage.getSortDirection().toUpperCase()), userPage.getSortBy()));
    }
}
