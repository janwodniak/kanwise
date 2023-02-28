package com.kanwise.report_service.service.job_executor.implementation.project;

import com.kanwise.report_service.configuration.spaces.SpacesNamesConfigurationProperties;
import com.kanwise.report_service.model.file.FileUploadStatus;
import com.kanwise.report_service.model.job_execution_details.common.ExecutionStatus;
import com.kanwise.report_service.model.job_execution_details.project.ProjectReportJobExecutionDetails;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.model.notification.email.EmailRequest;
import com.kanwise.report_service.service.job_executor.common.JobExecutorService;
import com.kanwise.report_service.service.notification.email.common.IEmailService;
import com.kanwise.report_service.service.report.common.ReportService;
import com.kanwise.report_service.service.report_data.common.ReportDataService;
import com.kanwise.report_service.service.spaces.common.ISpacesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.kanwise.report_service.constant.job.JobExecutionConstant.JOB_EXECUTION_SUCCESS_MESSAGE;
import static com.kanwise.report_service.model.notification.email.EmailMessageType.PERSONAL_REPORT;
import static java.time.LocalDateTime.now;
import static java.util.concurrent.CompletableFuture.completedFuture;

@Slf4j
@Service
public class ProjectReportJobExecutorService implements JobExecutorService<ProjectReportJobExecutionDetails, ProjectReportJobInformation> {

    private static final String DIRECTORY_PATTERN = "reports/%s/project/%s";
    private static final String FILE_NAME_PATTERN = "%s-project-report-%s.pdf";
    private static String SPACE_NAME;
    private final IEmailService emailService;
    private final ISpacesService spacesService;
    private final ReportService projectReportService;
    private final ReportDataService<ProjectReportJobInformation> projectReportDataService;

    @Autowired
    public ProjectReportJobExecutorService(IEmailService emailService, ISpacesService spacesService, SpacesNamesConfigurationProperties spacesNamesConfigurationProperties, ReportService projectReportService, ReportDataService<ProjectReportJobInformation> projectReportDataService) {
        this.emailService = emailService;
        this.spacesService = spacesService;
        this.projectReportService = projectReportService;
        this.projectReportDataService = projectReportDataService;
        SPACE_NAME = spacesNamesConfigurationProperties.reports();
    }

    @Override
    public CompletableFuture<ProjectReportJobExecutionDetails> execute(ProjectReportJobInformation jobInformation) {
        Map<String, Object> data = projectReportDataService.getReportData(jobInformation);

        String fileName = constructFileName(jobInformation);
        String directoryPath = constructDirectoryPath(jobInformation);
        String reportUrl = constructReportUrl(fileName, directoryPath);

        MultipartFile generatedReport = projectReportService.generateReport(data, fileName);
        uploadFile(generatedReport, directoryPath).thenAccept(handleFileUploadResult(data, fileName, reportUrl));

        return completedFuture(constructJobExecutionDetails(jobInformation, reportUrl));
    }

    private Consumer<FileUploadStatus> handleFileUploadResult(Map<String, Object> data, String fileName, String reportUrl) {
        return uploadStatus -> {
            if (uploadStatus.isSuccessful()) {
                sendEmail(reportUrl, data);
                try {
                    projectReportService.removeReport(fileName);
                } catch (IOException e) {
                    log.error("Error while removing generatedReport file", e);
                }
            }
        };
    }

    private void sendEmail(String reportUrl, Map<String, Object> data) {
        emailService.sendEmail(constructEmailRequest(reportUrl, data));
    }


    private CompletableFuture<FileUploadStatus> uploadFile(MultipartFile report, String formatted) {
        return spacesService.uploadFile(report, formatted, SPACE_NAME);
    }

    private String constructReportUrl(String fileName, String directoryPath) {
        return checkIfDirectoryExists(SPACE_NAME, directoryPath) + fileName;
    }

    private String constructDirectoryPath(ProjectReportJobInformation jobInformation) {
        return DIRECTORY_PATTERN.formatted(jobInformation.getSubscriber().getUsername(), jobInformation.getProjectId());
    }

    private URL checkIfDirectoryExists(String spaceName, String formatted) {
        return spacesService.checkIfDirectoryExists(spaceName, formatted, true);
    }

    private ProjectReportJobExecutionDetails constructJobExecutionDetails(ProjectReportJobInformation jobInformation, String reportUrl) {
        return ProjectReportJobExecutionDetails.builder()
                .executionStatus(ExecutionStatus.SUCCESS)
                .message(JOB_EXECUTION_SUCCESS_MESSAGE)
                .jobInformation(jobInformation)
                .reportUrl(reportUrl)
                .build();
    }

    private EmailRequest constructEmailRequest(String reportUrl, Map<String, Object> data) {
        return EmailRequest.builder()
                .to(data.get("email").toString())
                .subject("Project Report")
                .isHtml(true)
                .data(Map.of("firstName", data.get("firstName").toString(),
                        "reportStartDate", now(),
                        "reportEndDate", now(),
                        "reportType", "personal",
                        "href", reportUrl))
                .type(PERSONAL_REPORT)
                .build();
    }

    private String constructFileName(ProjectReportJobInformation jobInformationService) {
        return FILE_NAME_PATTERN.formatted(jobInformationService.getUsername(), now());
    }
}
