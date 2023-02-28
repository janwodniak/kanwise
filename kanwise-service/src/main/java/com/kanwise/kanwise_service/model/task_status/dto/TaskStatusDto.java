package com.kanwise.kanwise_service.model.task_status.dto;

import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class TaskStatusDto extends RepresentationModel<TaskStatusDto> {
    long id;
    long taskId;
    boolean ongoing;
    TaskStatusLabel label;
    LocalDateTime setAt;
    LocalDateTime setTill;
    String setBy;
}
