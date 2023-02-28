package com.kanwise.kanwise_service.service.statistics.member.implementation;

import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.member_statistics.MemberStatistics;
import com.kanwise.kanwise_service.model.member_statistics.constaraint.MemberStatisticsConstraints;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_statistics.TaskStatistics;
import com.kanwise.kanwise_service.service.member.IMemberService;
import com.kanwise.kanwise_service.service.statistics.TaskStatisticsCalculator;
import com.kanwise.kanwise_service.service.statistics.member.IMemberStatisticsService;
import com.kanwise.kanwise_service.service.statistics.task.ITaskStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kanwise.kanwise_service.model.task_status.TaskStatusLabel.IN_PROGRESS;

@RequiredArgsConstructor
@Service
public class MemberStatisticsService implements IMemberStatisticsService, TaskStatisticsCalculator {

    private final IMemberService memberService;
    private final ITaskStatisticsService taskStatisticsService;


    @Override
    public MemberStatistics findStatisticsForMember(String username, MemberStatisticsConstraints constraints) {
        Member member = memberService.findMemberByUsername(username);
        Set<Task> tasks = memberService.findTasksForMember(username);
        Set<Project> projects = memberService.findProjectsForMember(username);
        Set<TaskStatistics> tasksStatistics = getTaskStatistics(constraints, tasks);
        return generateMemberStatistics(member, tasks, tasksStatistics, projects);
    }

    private Set<TaskStatistics> getTaskStatistics(MemberStatisticsConstraints constraints, Set<Task> tasksForMember) {
        return tasksForMember.stream()
                .filter(getPeriodConstraints(constraints))
                .map(taskStatisticsService::getTaskStatistics)
                .collect(Collectors.toSet());
    }

    private MemberStatistics generateMemberStatistics(Member member, Set<Task> tasks, Set<TaskStatistics> taskStatistics, Set<Project> projects) {
        return MemberStatistics.builder()
                .member(member)
                .totalEstimatedTime(getTotalEstimatedTime(tasks))
                .totalTasksStatusCountMap(getTasksCountForTaskStatusLabel(tasks))
                .totalTasksStatusDurationMap(getTasksDurationsForTaskStatusLabels(taskStatistics))
                .totalTasksTypeCountMap(getTasksCountForTaskType(tasks))
                .totalTasksStatusCountByProjectMap(getTasksCountForProjectAndTaskStatusLabel(projects))
                .performancePercentage(getPerformancePercentage(getTotalEstimatedTime(tasks), getTasksDurationsForTaskStatusLabels(taskStatistics).get(IN_PROGRESS)))
                .build();
    }

    private Predicate<Task> getPeriodConstraints(MemberStatisticsConstraints constraints) {
        return task -> task.getCreatedAt()
                .isAfter(constraints.getStartDate()) && task.getCreatedAt().isBefore(constraints.getEndDate());
    }
}
