package com.kanwise.kanwise_service.model.task.mapping;

import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task.TaskPriority;
import com.kanwise.kanwise_service.model.task.TaskType;
import com.kanwise.kanwise_service.model.task.command.CreateTaskCommand;
import com.kanwise.kanwise_service.model.task_status.TaskStatus;
import com.kanwise.kanwise_service.service.member.IMemberService;
import com.kanwise.kanwise_service.service.project.IProjectService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Set;

import static com.kanwise.kanwise_service.model.task_status.TaskStatusLabel.valueOf;
import static java.lang.Boolean.TRUE;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
@Service
public class CreateTaskCommandToTaskConverter implements Converter<CreateTaskCommand, Task> {

    private final IMemberService memberService;
    private final IProjectService projectService;
    private final Clock clock;

    @Override
    public Task convert(MappingContext<CreateTaskCommand, Task> mappingContext) {
        CreateTaskCommand command = mappingContext.getSource();
        Task task = Task.builder()
                .title(command.title())
                .description(command.description())
                .estimatedTime(command.estimatedTime())
                .priority(TaskPriority.valueOf(command.priority().toUpperCase()))
                .type(TaskType.valueOf(command.type().toUpperCase()))
                .currentStatus(valueOf(command.currentStatus().toUpperCase()))
                .createdAt(now(clock))
                .active(TRUE)
                .build();

        Project project = projectService.findProjectById(command.projectId());

        task.setProject(project);

        Member member = memberService.findMemberByUsername(command.authorUsername());

        task.setAuthor(member);

        task.addMembers(getMembers(command.membersUsernames()));

        task.addStatus(TaskStatus.builder()
                .label(valueOf(command.currentStatus().toUpperCase()))
                .setAt(now(clock))
                .setBy(task.getAuthor().getMember())
                .build());

        return task;
    }

    private Set<Member> getMembers(Set<String> membersUsernames) {
        return membersUsernames.stream()
                .map(memberService::findMemberByUsername)
                .collect(toSet());
    }
}



