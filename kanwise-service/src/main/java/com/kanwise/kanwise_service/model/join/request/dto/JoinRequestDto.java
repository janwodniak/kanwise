package com.kanwise.kanwise_service.model.join.request.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class JoinRequestDto extends RepresentationModel<JoinRequestDto> {
    long id;
    long projectId;
    String requestedByUsername;
    Long joinResponseId;
    LocalDateTime requestedAt;
    String message;
}
