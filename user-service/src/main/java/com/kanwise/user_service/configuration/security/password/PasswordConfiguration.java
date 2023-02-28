package com.kanwise.user_service.configuration.security.password;

import org.passay.CharacterRule;
import org.passay.LengthRule;
import org.passay.PasswordGenerator;
import org.passay.PasswordValidator;
import org.passay.WhitespaceRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;

import static org.passay.EnglishCharacterData.Digit;
import static org.passay.EnglishCharacterData.LowerCase;
import static org.passay.EnglishCharacterData.Special;
import static org.passay.EnglishCharacterData.UpperCase;

@Configuration
public class PasswordConfiguration {
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PasswordValidator passwordValidator() {
        return new PasswordValidator(Arrays.asList(
                new LengthRule(8, 16),
                new WhitespaceRule(),
                new CharacterRule(UpperCase, 1),
                new CharacterRule(LowerCase, 1),
                new CharacterRule(Digit, 1),
                new CharacterRule(Special, 1)
        ));
    }

    @Bean
    public PasswordGenerator passwordGenerator() {
        return new PasswordGenerator();
    }
}
