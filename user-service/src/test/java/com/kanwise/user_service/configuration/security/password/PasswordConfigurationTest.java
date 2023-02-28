package com.kanwise.user_service.configuration.security.password;

import org.junit.jupiter.api.Test;
import org.passay.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = PasswordConfiguration.class)
@ActiveProfiles("test")
class PasswordConfigurationTest {


    private final PasswordConfiguration passwordConfiguration;
    private final ApplicationContext applicationContext;

    @Autowired
    PasswordConfigurationTest(PasswordConfiguration passwordConfiguration, ApplicationContext applicationContext) {
        this.passwordConfiguration = passwordConfiguration;
        this.applicationContext = applicationContext;
    }

    @Test
    void shouldPopulateBCryptPasswordEncoder() {
        // Given
        // When
        BCryptPasswordEncoder bCryptPasswordEncoder = passwordConfiguration.bCryptPasswordEncoder();
        BCryptPasswordEncoder bCryptPasswordEncoderBean = applicationContext.getBean(BCryptPasswordEncoder.class);
        // Then
        assertNotNull(bCryptPasswordEncoder);
        assertNotNull(bCryptPasswordEncoderBean);
        assertEquals(bCryptPasswordEncoder, bCryptPasswordEncoderBean);
    }

    @Bean
    void shouldPopulatePasswordValidator() {
        // Given
        // When
        PasswordValidator passwordValidator = passwordConfiguration.passwordValidator();
        PasswordValidator passwordValidatorBean = applicationContext.getBean(PasswordValidator.class);
        // Then
        assertNotNull(passwordValidator);
        assertNotNull(passwordValidatorBean);
        assertEquals(passwordValidator, passwordValidatorBean);
    }
}