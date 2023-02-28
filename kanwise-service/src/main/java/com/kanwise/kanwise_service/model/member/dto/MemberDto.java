package com.kanwise.kanwise_service.model.member.dto;

import com.kanwise.kanwise_service.model.notification.ProjectNotificationType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.RepresentationModel;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Builder
@Value
public class MemberDto extends RepresentationModel<MemberDto> {
    String username;
    int projectCount;
    int commentsCount;
    int tasksCount;
    Map<ProjectNotificationType, Boolean> notificationSubscriptions;
}
