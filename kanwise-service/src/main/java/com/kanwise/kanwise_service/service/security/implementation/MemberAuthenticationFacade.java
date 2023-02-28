package com.kanwise.kanwise_service.service.security.implementation;


import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.service.member.IMemberService;
import com.kanwise.kanwise_service.service.security.IMemberAuthenticationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.kanwise.kanwise_service.constant.SecurityConstant.ROLE_ADMIN;


@Component
@RequiredArgsConstructor
public class MemberAuthenticationFacade implements IMemberAuthenticationFacade {

    private final IMemberService memberService;

    @Override
    public boolean isAdmin() {
        return getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals(ROLE_ADMIN));
    }

    @Override
    public boolean isMemberByUsername(String username) {
        return getMember().map(member -> member.getUsername().equals(username)).orElse(false);
    }

    @Override
    public boolean isMemberByUsernameAndHasAuthority(String username, String authority) {
        return isMemberByUsername(username) && getAuthentication().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authority));
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Optional<Member> getMember() {
        return memberService.findMember(getUsername());
    }
}
