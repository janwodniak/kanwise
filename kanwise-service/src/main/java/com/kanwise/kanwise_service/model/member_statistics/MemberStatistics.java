package com.kanwise.kanwise_service.model.member_statistics;

import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.task.TaskType;
import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MemberStatistics {
    private Member member;
    private long performancePercentage;
    private Duration totalEstimatedTime;
    private Map<TaskStatusLabel, Long> totalTasksStatusCountMap;
    private Map<TaskType, Long> totalTasksTypeCountMap;
    private Map<TaskStatusLabel, Duration> totalTasksStatusDurationMap;
    private Map<String, EnumMap<TaskStatusLabel, Long>> totalTasksStatusCountByProjectMap;
}
