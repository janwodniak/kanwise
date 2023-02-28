package com.kanwise.kanwise_service.model.task_comment_reaction.mapping;

import com.kanwise.kanwise_service.controller.member.MemberController;
import com.kanwise.kanwise_service.controller.task.TaskCommentController;
import com.kanwise.kanwise_service.controller.task.TaskController;
import com.kanwise.kanwise_service.controller.task.TaskStatisticsController;
import com.kanwise.kanwise_service.controller.task.TaskStatusController;
import com.kanwise.kanwise_service.model.task_comment.command.CreateTaskCommentPageCommand;
import com.kanwise.kanwise_service.model.task_comment_reaction.TaskCommentReaction;
import com.kanwise.kanwise_service.model.task_comment_reaction.dto.TaskCommentReactionDto;
import com.kanwise.kanwise_service.model.task_status.command.CreateTaskStatusPageCommand;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class TaskCommentReactionToTaskCommentReactionDtoConverter implements Converter<TaskCommentReaction, TaskCommentReactionDto> {
    @Override
    public TaskCommentReactionDto convert(MappingContext<TaskCommentReaction, TaskCommentReactionDto> mappingContext) {
        TaskCommentReaction taskCommentReaction = mappingContext.getSource();
        TaskCommentReactionDto taskCommentReactionDto = TaskCommentReactionDto.builder()
                .id(taskCommentReaction.getId())
                .taskId(taskCommentReaction.getComment().getTask().getId())
                .authorUsername(taskCommentReaction.getAuthor().getUsername())
                .reactionLabel(taskCommentReaction.getReactionLabel().name())
                .reactedAt(taskCommentReaction.getReactedAt())
                .build();

        addHateoasLinks(taskCommentReaction, taskCommentReactionDto);
        return taskCommentReactionDto;
    }

    private void addHateoasLinks(TaskCommentReaction taskCommentReaction, TaskCommentReactionDto taskCommentReactionDto) {
        taskCommentReactionDto.add(linkTo(methodOn(MemberController.class).findMemberByUsername(taskCommentReaction.getComment().getAuthor().getMember().getUsername())).withRel("task-comment-author"));
        taskCommentReactionDto.add(linkTo(methodOn(MemberController.class).findMemberByUsername(taskCommentReaction.getAuthor().getUsername())).withRel("task-reaction-author"));
        taskCommentReactionDto.add(linkTo(methodOn(TaskController.class).findTask(taskCommentReaction.getId())).withRel("task"));
        taskCommentReactionDto.add(linkTo(methodOn(TaskController.class).findAssignedMembers(taskCommentReaction.getComment().getTask().getId())).withRel("assigned-members"));
        taskCommentReactionDto.add(linkTo(methodOn(TaskStatisticsController.class).findTaskStatistics(taskCommentReaction.getComment().getTask().getId())).withRel("statistics"));
        taskCommentReactionDto.add(linkTo(methodOn(TaskCommentController.class).findTaskCommentsForTask(taskCommentReaction.getComment().getTask().getId(), new CreateTaskCommentPageCommand())).withRel("comments"));
        taskCommentReactionDto.add(linkTo(methodOn(TaskStatusController.class).findTaskStatuses(taskCommentReaction.getComment().getTask().getId(), new CreateTaskStatusPageCommand())).withRel("statuses"));
    }
}
