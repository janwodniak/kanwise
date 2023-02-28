package com.kanwise.clients.kanwise_service.member.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    private String username;
    private int projectCount;
    private int commentsCount;
    private int tasksCount;
    private Map<String, Boolean> notificationSubscriptions;
}
