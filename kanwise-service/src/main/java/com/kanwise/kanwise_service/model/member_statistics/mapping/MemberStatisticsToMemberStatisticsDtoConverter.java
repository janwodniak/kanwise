package com.kanwise.kanwise_service.model.member_statistics.mapping;

import com.kanwise.kanwise_service.controller.member.MemberController;
import com.kanwise.kanwise_service.model.member_statistics.MemberStatistics;
import com.kanwise.kanwise_service.model.member_statistics.dto.MemberStatisticsDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class MemberStatisticsToMemberStatisticsDtoConverter implements Converter<MemberStatistics, MemberStatisticsDto> {

    @Override
    public MemberStatisticsDto convert(MappingContext<MemberStatistics, MemberStatisticsDto> mappingContext) {
        MemberStatistics memberStatistics = mappingContext.getSource();
        MemberStatisticsDto memberStatisticsDto = MemberStatisticsDto.builder()
                .memberUsername(memberStatistics.getMember().getUsername())
                .totalTasksCount(memberStatistics.getTotalTasksStatusCountMap().values().stream().mapToLong(Long::longValue).sum())
                .totalEstimatedTime(memberStatistics.getTotalEstimatedTime())
                .totalTasksStatusCountMap(memberStatistics.getTotalTasksStatusCountMap())
                .totalTasksStatusDurationMap(memberStatistics.getTotalTasksStatusDurationMap())
                .totalTasksTypeCountMap(memberStatistics.getTotalTasksTypeCountMap())
                .totalTasksStatusCountByProjectMap(memberStatistics.getTotalTasksStatusCountByProjectMap())
                .performancePercentage(memberStatistics.getPerformancePercentage())
                .build();

        addHateoasLinks(memberStatistics, memberStatisticsDto);
        return memberStatisticsDto;
    }

    private void addHateoasLinks(MemberStatistics memberStatistics, MemberStatisticsDto memberStatisticsDto) {
        memberStatisticsDto.add(linkTo(methodOn(MemberController.class).findProjectsForMember(memberStatistics.getMember().getUsername())).withRel("projects"));
        memberStatisticsDto.add(linkTo(methodOn(MemberController.class).findTasksForMember(memberStatistics.getMember().getUsername())).withRel("tasks"));
        memberStatisticsDto.add(linkTo(methodOn(MemberController.class).findTaskCommentsForMember(memberStatistics.getMember().getUsername())).withRel("task-comments"));
        memberStatisticsDto.add(linkTo(methodOn(MemberController.class).findJoinRequestsForMember(memberStatistics.getMember().getUsername())).withRel("join-requests"));
        memberStatisticsDto.add(linkTo(methodOn(MemberController.class).findJoinResponsesForMember(memberStatistics.getMember().getUsername())).withRel("join-responses"));
    }
}
