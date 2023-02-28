package com.kanwise.kanwise_service.model.task.mapping;

import com.kanwise.kanwise_service.controller.member.MemberController;
import com.kanwise.kanwise_service.controller.project.ProjectController;
import com.kanwise.kanwise_service.controller.task.TaskCommentController;
import com.kanwise.kanwise_service.controller.task.TaskController;
import com.kanwise.kanwise_service.controller.task.TaskStatisticsController;
import com.kanwise.kanwise_service.controller.task.TaskStatusController;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task.dto.TaskDto;
import com.kanwise.kanwise_service.model.task_comment.command.CreateTaskCommentPageCommand;
import com.kanwise.kanwise_service.model.task_status.command.CreateTaskStatusPageCommand;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class TaskToTaskDtoConverter implements Converter<Task, TaskDto> {
    @Override
    public TaskDto convert(MappingContext<Task, TaskDto> mappingContext) {
        Task task = mappingContext.getSource();

        TaskDto taskDto = TaskDto.builder()
                .taskId(task.getId())
                .projectId(task.getProject().getId())
                .authorUsername(task.getAuthor().getMember().getUsername())
                .title(task.getTitle())
                .description(task.getDescription())
                .estimatedTime(task.getEstimatedTime())
                .priority(task.getPriority())
                .type(task.getType())
                .assignedMembersCount(task.getAssignedMemberships().size())
                .commentsCount(task.getComments().size())
                .statusesCount(task.getStatuses().size())
                .currentStatus(task.getCurrentStatus())
                .build();

        addHateoasLinks(task, taskDto);
        return taskDto;
    }

    private void addHateoasLinks(Task task, TaskDto taskDto) {
        taskDto.add(linkTo(methodOn(ProjectController.class).findProjectById(task.getProject().getId())).withRel("project"));
        taskDto.add(linkTo(methodOn(MemberController.class).findMemberByUsername(task.getAuthor().getMember().getUsername())).withRel("author"));
        taskDto.add(linkTo(methodOn(TaskController.class).findAssignedMembers(task.getId())).withRel("assigned-members"));
        taskDto.add(linkTo(methodOn(TaskStatisticsController.class).findTaskStatistics(task.getId())).withRel("statistics"));
        taskDto.add(linkTo(methodOn(TaskCommentController.class).findTaskCommentsForTask(task.getId(), new CreateTaskCommentPageCommand())).withRel("comments"));
        taskDto.add(linkTo(methodOn(TaskStatusController.class).findTaskStatuses(task.getId(), new CreateTaskStatusPageCommand())).withRel("statuses"));
    }
}










