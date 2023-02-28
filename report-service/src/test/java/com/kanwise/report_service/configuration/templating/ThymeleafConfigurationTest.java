package com.kanwise.report_service.configuration.templating;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ThymeleafConfiguration.class)
@ActiveProfiles("test")
class ThymeleafConfigurationTest {

    private final ThymeleafConfiguration thymeleafConfiguration;
    private final ApplicationContext applicationContext;

    @Autowired
    public ThymeleafConfigurationTest(ThymeleafConfiguration thymeleafConfiguration, ApplicationContext applicationContext) {
        this.thymeleafConfiguration = thymeleafConfiguration;
        this.applicationContext = applicationContext;
    }

    @Test
    void shouldPopulateClassLoaderTemplateResolver() {
        // Given
        // When
        ClassLoaderTemplateResolver classLoaderTemplateResolver = thymeleafConfiguration.classLoaderTemplateResolver();
        ClassLoaderTemplateResolver classLoaderTemplateResolverBean = applicationContext.getBean(ClassLoaderTemplateResolver.class);
        // Then
        assertNotNull(classLoaderTemplateResolverBean);
        assertNotNull(classLoaderTemplateResolver);
        assertEquals("templates-report-pdf/", classLoaderTemplateResolver.getPrefix());
        assertEquals(".html", classLoaderTemplateResolver.getSuffix());
        assertEquals("UTF-8", classLoaderTemplateResolver.getCharacterEncoding());
        assertEquals(1, classLoaderTemplateResolver.getOrder());
        assertEquals(classLoaderTemplateResolver, classLoaderTemplateResolverBean);
    }

    @Test
    void shouldPopulateTemplateEngine() {
        // Given
        // When
        TemplateEngine templateEngine = thymeleafConfiguration.templateEngine(thymeleafConfiguration.classLoaderTemplateResolver());
        TemplateEngine templateEngineBean = applicationContext.getBean(TemplateEngine.class);
        // Then
        assertNotNull(templateEngineBean);
        assertNotNull(templateEngine);
        assertEquals(templateEngine, templateEngineBean);
    }
}