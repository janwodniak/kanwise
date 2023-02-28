package com.kanwise.kanwise_service.model.project.mapping;

import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.project.command.CreateProjectCommand;
import com.kanwise.kanwise_service.service.member.IMemberService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Set;

import static com.kanwise.kanwise_service.model.project.ProjectStatus.CREATED;
import static java.lang.Boolean.TRUE;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
@Service
public class CreateProjectCommandToProjectConverter implements Converter<CreateProjectCommand, Project> {

    private final IMemberService memberService;
    private final Clock clock;

    @Override
    public Project convert(MappingContext<CreateProjectCommand, Project> mappingContext) {
        CreateProjectCommand command = mappingContext.getSource();
        Project project = Project.builder()
                .title(command.title())
                .description(command.description())
                .createdAt(now(clock))
                .active(TRUE)
                .status(CREATED)
                .build();
        project.addMembers(getMembers(command.membersUsernames()));
        return project;
    }

    private Set<Member> getMembers(Set<String> membersUsernames) {
        return membersUsernames.stream()
                .map(memberService::findMemberByUsername)
                .collect(toSet());
    }
}

