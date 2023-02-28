package com.kanwise.kanwise_service.model.task_status.mapping;

import com.kanwise.kanwise_service.model.task_status.command.CreateTaskStatusPageCommand;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static org.springframework.data.domain.Sort.by;

@Service
public class CreateTaskStatusPageToPageableConverter implements Converter<CreateTaskStatusPageCommand, Pageable> {
    @Override
    public Pageable convert(MappingContext<CreateTaskStatusPageCommand, Pageable> mappingContext) {
        CreateTaskStatusPageCommand taskStatusPage = mappingContext.getSource();
        return PageRequest.of(taskStatusPage.getPageNumber(),
                taskStatusPage.getPageSize(),
                by(Sort.Direction.valueOf(taskStatusPage.getSortDirection().toUpperCase()), taskStatusPage.getSortBy()));
    }
}
