package com.kanwise.kanwise_service.model.project.mapping;

import com.kanwise.kanwise_service.model.project.command.CreateProjectPageCommand;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static org.springframework.data.domain.Sort.by;

@Service
public class CreateProjectPageCommandToPageableConverter implements Converter<CreateProjectPageCommand, Pageable> {
    @Override
    public Pageable convert(MappingContext<CreateProjectPageCommand, Pageable> mappingContext) {
        CreateProjectPageCommand projectPage = mappingContext.getSource();
        return PageRequest.of(projectPage.getPageNumber(),
                projectPage.getPageSize(),
                by(Sort.Direction.valueOf(projectPage.getSortDirection().toUpperCase()), projectPage.getSortBy()));
    }
}
