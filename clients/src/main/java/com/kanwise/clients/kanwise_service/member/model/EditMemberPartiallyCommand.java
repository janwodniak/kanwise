package com.kanwise.clients.kanwise_service.member.model;

import lombok.Builder;

@Builder
public record EditMemberPartiallyCommand(String username) {
}
