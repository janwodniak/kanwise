package com.kanwise.kanwise_service.model.report.personal;

import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.report.ReportData;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_statistics.TaskStatistics;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Setter
@Getter
@SuperBuilder
public class PersonalReportData extends ReportData {
    private Member member;
    private Map<Task, TaskStatistics> tasks;
}
