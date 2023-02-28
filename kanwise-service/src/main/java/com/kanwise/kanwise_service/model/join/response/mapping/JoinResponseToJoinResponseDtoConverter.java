package com.kanwise.kanwise_service.model.join.response.mapping;

import com.kanwise.kanwise_service.controller.join.request.JoinRequestController;
import com.kanwise.kanwise_service.controller.member.MemberController;
import com.kanwise.kanwise_service.model.join.response.JoinResponse;
import com.kanwise.kanwise_service.model.join.response.dto.JoinResponseDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class JoinResponseToJoinResponseDtoConverter implements Converter<JoinResponse, JoinResponseDto> {
    @Override
    public JoinResponseDto convert(MappingContext<JoinResponse, JoinResponseDto> mappingContext) {
        JoinResponse joinResponse = mappingContext.getSource();
        JoinResponseDto joinResponseDto = JoinResponseDto.builder()
                .id(joinResponse.getId())
                .message(joinResponse.getMessage())
                .respondedByUsername(joinResponse.getRespondedBy().getUsername())
                .joinRequestId(joinResponse.getJoinRequest().getId())
                .message(joinResponse.getMessage())
                .respondedAt(joinResponse.getRespondedAt())
                .status(joinResponse.getStatus())
                .build();

        setHateoasLinks(joinResponse, joinResponseDto);
        return joinResponseDto;
    }

    private void setHateoasLinks(JoinResponse joinResponse, JoinResponseDto joinResponseDto) {
        joinResponseDto.add(linkTo(methodOn(MemberController.class).findMemberByUsername(joinResponse.getRespondedBy().getUsername())).withRel("responded-by"));
        joinResponseDto.add(linkTo(methodOn(JoinRequestController.class).findJoinRequestById(joinResponse.getJoinRequest().getId())).withRel("join-request"));
    }
}
