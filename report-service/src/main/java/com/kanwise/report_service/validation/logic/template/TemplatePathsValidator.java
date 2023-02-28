package com.kanwise.report_service.validation.logic.template;


import com.kanwise.report_service.configuration.templating.DirectoryConfigurationProperties;
import com.kanwise.report_service.model.report.ReportType;
import com.kanwise.report_service.validation.annotation.template.TemplatePaths;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Scope("prototype")
public class TemplatePathsValidator implements ConstraintValidator<TemplatePaths, Map<ReportType, String>> {

    private final DirectoryConfigurationProperties directoryConfigurationProperties;
    private String templatesDirectory;

    private static boolean isPresent(File[] files, String templatePath) {
        return Arrays.stream(files).anyMatch(file -> file.getName().equals(templatePath));
    }

    @Override
    public void initialize(TemplatePaths constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.templatesDirectory = directoryConfigurationProperties.reportTemplates();
    }

    @Override
    public boolean isValid(Map<ReportType, String> emailMessageTypeStringMap, ConstraintValidatorContext constraintValidatorContext) {
        return Optional.ofNullable(new File(templatesDirectory).listFiles())
                .map(files -> validateFilesPresence(emailMessageTypeStringMap, files))
                .orElse(false);
    }

    private boolean validateFilesPresence(Map<ReportType, String> emailMessageTypeStringMap, File[] files) {
        return emailMessageTypeStringMap.values().stream()
                .allMatch(templatePath -> isPresent(files, templatePath));
    }
}
