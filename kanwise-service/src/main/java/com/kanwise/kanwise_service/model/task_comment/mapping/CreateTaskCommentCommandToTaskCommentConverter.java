package com.kanwise.kanwise_service.model.task_comment.mapping;

import com.kanwise.kanwise_service.model.task_comment.TaskComment;
import com.kanwise.kanwise_service.model.task_comment.command.CreateTaskCommentCommand;
import com.kanwise.kanwise_service.service.member.IMemberService;
import com.kanwise.kanwise_service.service.task.ITaskService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.time.Clock;

import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Service
public class CreateTaskCommentCommandToTaskCommentConverter implements Converter<CreateTaskCommentCommand, TaskComment> {

    private final IMemberService memberService;
    private final ITaskService taskService;
    private final Clock clock;

    @Override
    public TaskComment convert(MappingContext<CreateTaskCommentCommand, TaskComment> mappingContext) {
        CreateTaskCommentCommand command = mappingContext.getSource();
        TaskComment taskComment = TaskComment.builder()
                .content(command.content())
                .commentedAt(now(clock))
                .build();

        taskComment.setTask(taskService.findTaskById(command.taskId()));
        taskComment.setAuthor(memberService.findMemberByUsername(command.authorUsername()));
        return taskComment;
    }
}
