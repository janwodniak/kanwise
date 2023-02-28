package com.kanwise.kanwise_service.model.project.dto;

import com.kanwise.kanwise_service.model.project.ProjectStatus;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class ProjectDto extends RepresentationModel<ProjectDto> {
    long id;
    String title;
    String description;
    LocalDateTime createdAt;
    long membersCount;
    long tasksCount;
    long todoTaskCount;
    long inProgressTaskCount;
    long doneTaskCount;
    long joinRequestsCount;
    ProjectStatus status;
}
