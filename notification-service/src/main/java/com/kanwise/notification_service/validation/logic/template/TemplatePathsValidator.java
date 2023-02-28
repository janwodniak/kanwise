package com.kanwise.notification_service.validation.logic.template;

import com.kanwise.notification_service.configuration.email.DirectoryConfigurationProperties;
import com.kanwise.notification_service.model.email.EmailMessageType;
import com.kanwise.notification_service.validation.annotation.template.TemplatePaths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
@Scope("prototype")
public class TemplatePathsValidator implements ConstraintValidator<TemplatePaths, Map<EmailMessageType, String>> {

    private final DirectoryConfigurationProperties directoryConfigurationProperties;
    private String templatesDirectory;

    @Override
    public void initialize(TemplatePaths constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.templatesDirectory = directoryConfigurationProperties.emailTemplates();
    }

    @Override
    public boolean isValid(Map<EmailMessageType, String> emailMessageTypeStringMap, ConstraintValidatorContext constraintValidatorContext) {
        return Optional.ofNullable(new File(templatesDirectory).listFiles())
                .map(files -> validateFilesPresence(emailMessageTypeStringMap, files))
                .orElse(false);
    }

    private boolean validateFilesPresence(Map<EmailMessageType, String> emailMessageTypeStringMap, File[] files) {
        return emailMessageTypeStringMap.values().stream().allMatch(templatePath -> Arrays.stream(files).anyMatch(file -> file.getName().equals(templatePath)));
    }
}
