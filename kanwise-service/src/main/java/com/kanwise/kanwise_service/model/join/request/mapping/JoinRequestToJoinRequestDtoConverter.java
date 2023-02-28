package com.kanwise.kanwise_service.model.join.request.mapping;


import com.kanwise.kanwise_service.controller.member.MemberController;
import com.kanwise.kanwise_service.controller.project.ProjectController;
import com.kanwise.kanwise_service.model.join.request.JoinRequest;
import com.kanwise.kanwise_service.model.join.request.dto.JoinRequestDto;
import com.kanwise.kanwise_service.model.join.response.JoinResponse;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class JoinRequestToJoinRequestDtoConverter implements Converter<JoinRequest, JoinRequestDto> {
    private static void setHateoasLinks(JoinRequest joinRequest, JoinRequestDto joinRequestDto) {
        joinRequestDto.add(linkTo(methodOn(MemberController.class).findMemberByUsername(joinRequest.getRequestedBy().getUsername())).withRel("requested-by"));
        joinRequestDto.add(linkTo(methodOn(ProjectController.class).findProjectById(joinRequest.getProject().getId())).withRel("project"));
        if (joinRequest.isResponded()) {
            joinRequestDto.add(linkTo(methodOn(MemberController.class).findMemberByUsername(joinRequest.getJoinResponse().getRespondedBy().getUsername())).withRel("responded-by"));
        }
    }

    @Override
    public JoinRequestDto convert(MappingContext<JoinRequest, JoinRequestDto> mappingContext) {
        JoinRequest joinRequest = mappingContext.getSource();
        JoinRequestDto joinRequestDto = JoinRequestDto.builder()
                .id(joinRequest.getId())
                .projectId(joinRequest.getProject().getId())
                .requestedByUsername(joinRequest.getRequestedBy().getUsername())
                .joinResponseId(ofNullable(joinRequest.getJoinResponse()).map(JoinResponse::getId).orElse(null))
                .message(joinRequest.getMessage())
                .requestedAt(joinRequest.getRequestedAt())
                .build();

        setHateoasLinks(joinRequest, joinRequestDto);
        return joinRequestDto;
    }
}
