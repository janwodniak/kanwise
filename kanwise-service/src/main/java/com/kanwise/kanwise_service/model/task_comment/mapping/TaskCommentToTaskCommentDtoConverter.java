package com.kanwise.kanwise_service.model.task_comment.mapping;

import com.kanwise.kanwise_service.controller.member.MemberController;
import com.kanwise.kanwise_service.controller.task.TaskCommentController;
import com.kanwise.kanwise_service.controller.task.TaskController;
import com.kanwise.kanwise_service.controller.task.TaskStatisticsController;
import com.kanwise.kanwise_service.controller.task.TaskStatusController;
import com.kanwise.kanwise_service.model.task_comment.TaskComment;
import com.kanwise.kanwise_service.model.task_comment.command.CreateTaskCommentPageCommand;
import com.kanwise.kanwise_service.model.task_comment.dto.TaskCommentDto;
import com.kanwise.kanwise_service.model.task_status.command.CreateTaskStatusPageCommand;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class TaskCommentToTaskCommentDtoConverter implements Converter<TaskComment, TaskCommentDto> {
    @Override
    public TaskCommentDto convert(MappingContext<TaskComment, TaskCommentDto> mappingContext) {
        TaskComment taskComment = mappingContext.getSource();
        TaskCommentDto taskCommentDto = TaskCommentDto.builder()
                .id(taskComment.getId())
                .authorUsername(taskComment.getAuthor().getMember().getUsername())
                .taskId(taskComment.getTask().getId())
                .content(taskComment.getContent())
                .commentedAt(taskComment.getCommentedAt())
                .likesCount(taskComment.getLikesCount())
                .dislikesCount(taskComment.getDislikesCount())
                .build();

        addHateoasLinks(taskComment, taskCommentDto);
        return taskCommentDto;
    }

    private void addHateoasLinks(TaskComment taskComment, TaskCommentDto taskCommentDto) {
        taskCommentDto.add(linkTo(methodOn(MemberController.class).findMemberByUsername(taskComment.getAuthor().getMember().getUsername())).withRel("task-comment-author"));
        taskCommentDto.add(linkTo(methodOn(TaskController.class).findTask(taskComment.getTask().getId())).withRel("task"));
        taskCommentDto.add(linkTo(methodOn(TaskController.class).findAssignedMembers(taskComment.getTask().getId())).withRel("assigned-members"));
        taskCommentDto.add(linkTo(methodOn(TaskStatisticsController.class).findTaskStatistics(taskComment.getTask().getId())).withRel("statistics"));
        taskCommentDto.add(linkTo(methodOn(TaskCommentController.class).findTaskCommentsForTask(taskComment.getTask().getId(), new CreateTaskCommentPageCommand())).withRel("comments"));
        taskCommentDto.add(linkTo(methodOn(TaskStatusController.class).findTaskStatuses(taskComment.getTask().getId(), new CreateTaskStatusPageCommand())).withRel("statuses"));
    }
}
