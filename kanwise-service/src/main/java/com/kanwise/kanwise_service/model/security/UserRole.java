package com.kanwise.kanwise_service.model.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;

import static com.kanwise.kanwise_service.model.security.UserPermission.MEMBER_DELETE;
import static com.kanwise.kanwise_service.model.security.UserPermission.MEMBER_READ;
import static com.kanwise.kanwise_service.model.security.UserPermission.MEMBER_WRITE;
import static com.kanwise.kanwise_service.model.security.UserPermission.PROJECT_DELETE;
import static com.kanwise.kanwise_service.model.security.UserPermission.PROJECT_READ;
import static com.kanwise.kanwise_service.model.security.UserPermission.PROJECT_WRITE;
import static com.kanwise.kanwise_service.model.security.UserPermission.TASK_DELETE;
import static com.kanwise.kanwise_service.model.security.UserPermission.TASK_READ;
import static com.kanwise.kanwise_service.model.security.UserPermission.TASK_WRITE;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;


@Getter
@AllArgsConstructor
public enum UserRole {
    USER(new HashSet<>(asList(MEMBER_READ, MEMBER_WRITE, PROJECT_READ, PROJECT_WRITE, TASK_READ, TASK_WRITE))),
    ADMIN(new HashSet<>(asList(MEMBER_READ, MEMBER_WRITE, MEMBER_DELETE, PROJECT_READ, PROJECT_WRITE, PROJECT_DELETE, TASK_READ, TASK_WRITE, TASK_DELETE)));

    private final Set<UserPermission> permissions;

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        Set<SimpleGrantedAuthority> authorities = getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.name()))
                .collect(toSet());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
