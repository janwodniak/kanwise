package com.kanwise.report_service.configuration.security.common;


import com.kanwise.report_service.configuration.security.filter.CustomAuthenticationProcessingFilter;
import com.kanwise.report_service.configuration.security.url.UrlConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import static java.util.Locale.ENGLISH;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class ApplicationSecurityConfiguration {
    private final CustomAuthenticationProcessingFilter customAuthenticationProcessingFilter;
    private final UrlConfigurationProperties urlConfigurationProperties;
    private final AccessDeniedHandler customAccessDeniedHandler;
    private final Http403ForbiddenEntryPoint customAuthenticationEntryPoint;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        configureHttpSecurity(http);
        return http.build();
    }

    private void configureHttpSecurity(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors()
                .and()
                .sessionManagement().sessionCreationPolicy(STATELESS)
                .and()
                .authorizeRequests().antMatchers(urlConfigurationProperties.publicUrlsAsArray()).permitAll()
                .and()
                .exceptionHandling().accessDeniedHandler(customAccessDeniedHandler)
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .and()
                .addFilterBefore(customAuthenticationProcessingFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
        sessionLocaleResolver.setDefaultLocale(ENGLISH);
        return sessionLocaleResolver;
    }
}
