package com.kanwise.kanwise_service.service.report.implementation.personal;

import com.kanwise.clients.report_service.report.model.personal.PersonalReportDataRequest;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.report.personal.PersonalReportData;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_statistics.TaskStatistics;
import com.kanwise.kanwise_service.service.member.implementation.MemberService;
import com.kanwise.kanwise_service.service.report.ReportDataService;
import com.kanwise.kanwise_service.service.statistics.task.implementaion.TaskStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Service
public class PersonalReportDataService implements ReportDataService<PersonalReportData, PersonalReportDataRequest> {

    private final MemberService memberService;
    private final TaskStatisticsService taskStatisticsService;

    private static List<Task> getTaskForMemberInTimeRange(Member member, LocalDateTime startDate, LocalDateTime endDate) {
        return member.getAssignedTasks()
                .stream()
                .filter(task -> task.getCreatedAt().isAfter(startDate) && task.getCreatedAt().isBefore(endDate))
                .toList();
    }

    @Override
    public PersonalReportData getReportData(PersonalReportDataRequest request) {
        Member member = memberService.findMemberByUsername(request.username());
        LocalDateTime startDate = request.startDate();
        LocalDateTime endDate = request.endDate();
        List<Task> tasks = getTaskForMemberInTimeRange(member, startDate, endDate);

        return PersonalReportData.builder()
                .startDate(startDate)
                .endDate(endDate)
                .member(member)
                .tasks(generateTaskStatisticsMap(tasks))
                .build();
    }

    private Map<Task, TaskStatistics> generateTaskStatisticsMap(List<Task> tasks) {
        return tasks
                .stream()
                .collect(toMap(task -> task, taskStatisticsService::getTaskStatistics));
    }
}
