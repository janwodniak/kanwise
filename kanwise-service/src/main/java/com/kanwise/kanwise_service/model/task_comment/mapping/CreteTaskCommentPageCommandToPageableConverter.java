package com.kanwise.kanwise_service.model.task_comment.mapping;

import com.kanwise.kanwise_service.model.task_comment.command.CreateTaskCommentPageCommand;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static org.springframework.data.domain.Sort.by;

@Service
public class CreteTaskCommentPageCommandToPageableConverter implements Converter<CreateTaskCommentPageCommand, Pageable> {
    @Override
    public Pageable convert(MappingContext<CreateTaskCommentPageCommand, Pageable> mappingContext) {
        CreateTaskCommentPageCommand taskCommentPage = mappingContext.getSource();
        return PageRequest.of(taskCommentPage.getPageNumber(), taskCommentPage.getPageSize(), by(Sort.Direction.valueOf(taskCommentPage.getSortDirection().toUpperCase()), taskCommentPage.getSortBy()));
    }
}
