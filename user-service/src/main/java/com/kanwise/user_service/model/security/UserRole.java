package com.kanwise.user_service.model.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;

import static com.kanwise.user_service.model.security.UserPermission.IMAGE_WRITE;
import static com.kanwise.user_service.model.security.UserPermission.PASSWORD_WRITE;
import static com.kanwise.user_service.model.security.UserPermission.USER_DELETE;
import static com.kanwise.user_service.model.security.UserPermission.USER_READ;
import static com.kanwise.user_service.model.security.UserPermission.USER_WRITE;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;


@Getter
@AllArgsConstructor
public enum UserRole {
    USER(new HashSet<>(asList(USER_READ, USER_WRITE, PASSWORD_WRITE, IMAGE_WRITE))),
    ADMIN(new HashSet<>(asList(USER_READ, USER_WRITE, USER_DELETE, PASSWORD_WRITE, IMAGE_WRITE)));

    private final Set<UserPermission> permissions;

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        Set<SimpleGrantedAuthority> authorities = getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.name()))
                .collect(toSet());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
