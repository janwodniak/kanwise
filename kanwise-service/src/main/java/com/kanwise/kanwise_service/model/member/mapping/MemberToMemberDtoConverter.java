package com.kanwise.kanwise_service.model.member.mapping;

import com.kanwise.kanwise_service.controller.member.MemberController;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.member.dto.MemberDto;
import com.kanwise.kanwise_service.model.member_statistics.constaraint.MemberStatisticsConstraints;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class MemberToMemberDtoConverter implements Converter<Member, MemberDto> {

    private static void addHateoasLinks(Member member, MemberDto memberDto) {
        memberDto.add(linkTo(methodOn(MemberController.class).findProjectsForMember(member.getUsername())).withRel("projects"));
        memberDto.add(linkTo(methodOn(MemberController.class).findTasksForMember(member.getUsername())).withRel("tasks"));
        memberDto.add(linkTo(methodOn(MemberController.class).findTaskCommentsForMember(member.getUsername())).withRel("task-comments"));
        memberDto.add(linkTo(methodOn(MemberController.class).findJoinRequestsForMember(member.getUsername())).withRel("join-requests"));
        memberDto.add(linkTo(methodOn(MemberController.class).findJoinResponsesForMember(member.getUsername())).withRel("join-responses"));
        memberDto.add(linkTo(methodOn(MemberController.class).findStatisticsForMember(member.getUsername(), new MemberStatisticsConstraints())).withRel("statistics"));
    }

    @Override
    public MemberDto convert(MappingContext<Member, MemberDto> mappingContext) {
        Member member = mappingContext.getSource();
        MemberDto memberDto = MemberDto.builder()
                .username(member.getUsername())
                .projectCount(member.getProjects().size())
                .commentsCount(member.getTaskComments().size())
                .tasksCount(member.getAssignedTasks().size())
                .notificationSubscriptions(member.getNotificationSubscriptions())
                .build();

        addHateoasLinks(member, memberDto);
        return memberDto;
    }

}
