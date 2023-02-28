package com.kanwise.kanwise_service.validation.logic.member;

import com.kanwise.kanwise_service.service.member.IMemberService;
import com.kanwise.kanwise_service.validation.annotation.member.UniqueUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    private final IMemberService memberService;

    public boolean isValid(String username, ConstraintValidatorContext context) {
        return Optional.ofNullable(username)
                .map(u -> !memberService.existsByUsername(u))
                .orElse(true);
    }
}
