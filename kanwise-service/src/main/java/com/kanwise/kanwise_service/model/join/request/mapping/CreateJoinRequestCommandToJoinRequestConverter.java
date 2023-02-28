package com.kanwise.kanwise_service.model.join.request.mapping;


import com.kanwise.kanwise_service.model.join.request.JoinRequest;
import com.kanwise.kanwise_service.model.join.request.command.CreateJoinRequestCommand;
import com.kanwise.kanwise_service.service.member.IMemberService;
import com.kanwise.kanwise_service.service.project.IProjectService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.time.Clock;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class CreateJoinRequestCommandToJoinRequestConverter implements Converter<CreateJoinRequestCommand, JoinRequest> {


    private final IMemberService memberService;
    private final IProjectService projectService;
    private final Clock clock;

    @Override
    public JoinRequest convert(MappingContext<CreateJoinRequestCommand, JoinRequest> mappingContext) {
        CreateJoinRequestCommand command = mappingContext.getSource();
        JoinRequest joinRequest = JoinRequest.builder()
                .message(command.message())
                .requestedAt(now(clock))
                .build();

        joinRequest.setProject(projectService.findProjectById(command.projectId()));
        joinRequest.setRequestedBy(memberService.findMemberByUsername(command.requestedByUsername()));

        return joinRequest;
    }
}
