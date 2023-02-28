package com.kanwise.kanwise_service.model.join.response.dto;

import com.kanwise.kanwise_service.model.join.request.JoinRequestStatus;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;


@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class JoinResponseDto extends RepresentationModel<JoinResponseDto> {
    long id;
    String respondedByUsername;
    long joinRequestId;
    JoinRequestStatus status;
    String message;
    LocalDateTime respondedAt;
}
