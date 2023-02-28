package com.kanwise.user_service.configuration.security.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.LocaleResolver;

import static java.util.Locale.ENGLISH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test-kafka-disabled")
class ApplicationSecurityConfigurationTest {

    private final ApplicationSecurityConfiguration applicationSecurityConfiguration;
    private final ApplicationContext applicationContext;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final HttpSecurity httpSecurity;

    @Autowired
    ApplicationSecurityConfigurationTest(ApplicationSecurityConfiguration applicationSecurityConfiguration, ApplicationContext applicationContext, AuthenticationConfiguration authenticationConfiguration, HttpSecurity httpSecurity) {
        this.applicationSecurityConfiguration = applicationSecurityConfiguration;
        this.applicationContext = applicationContext;
        this.authenticationConfiguration = authenticationConfiguration;
        this.httpSecurity = httpSecurity;
    }

    @Test
    void shouldPopulateSecurityFilterChain() throws Exception {
        // Given
        // When
        SecurityFilterChain securityFilterChain = applicationSecurityConfiguration.filterChain(httpSecurity);
        SecurityFilterChain securityFilterChainBean = applicationContext.getBean(SecurityFilterChain.class);
        // Then
        assertNotNull(securityFilterChain);
        assertNotNull(securityFilterChainBean);
        assertEquals(securityFilterChain, securityFilterChainBean);
    }

    @Test
    void shouldPopulateAuthenticationManager() throws Exception {
        // Given
        // When
        AuthenticationManager authenticationManager = applicationSecurityConfiguration.authenticationManager(authenticationConfiguration);
        AuthenticationManager authenticationManagerBean = applicationContext.getBean(AuthenticationManager.class);
        // Then
        assertNotNull(authenticationManagerBean);
        assertNotNull(authenticationManager);
        assertEquals(authenticationManagerBean, authenticationManager);
    }

    @Test
    void shouldPopulateLocaleResolver() {
        // Given
        // When
        LocaleResolver localeResolver = applicationSecurityConfiguration.localeResolver();
        LocaleResolver localeResolverBean = applicationContext.getBean(LocaleResolver.class);
        // Then
        assertNotNull(localeResolverBean);
        assertNotNull(localeResolver);
        assertEquals(ENGLISH, localeResolver.resolveLocale(new MockHttpServletRequest()));
        assertEquals(localeResolverBean, localeResolver);
    }
}