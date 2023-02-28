package com.kanwise.report_service.service.job_executor.implementation.personal;

import com.kanwise.report_service.configuration.spaces.SpacesNamesConfigurationProperties;
import com.kanwise.report_service.model.file.FileUploadStatus;
import com.kanwise.report_service.model.job_execution_details.personal.PersonalReportJobExecutionDetails;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.notification.email.EmailRequest;
import com.kanwise.report_service.service.job_executor.common.JobExecutorService;
import com.kanwise.report_service.service.notification.email.common.IEmailService;
import com.kanwise.report_service.service.report.common.ReportService;
import com.kanwise.report_service.service.report_data.common.ReportDataService;
import com.kanwise.report_service.service.spaces.common.ISpacesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.kanwise.report_service.constant.job.JobExecutionConstant.JOB_EXECUTION_SUCCESS_MESSAGE;
import static com.kanwise.report_service.model.job_execution_details.common.ExecutionStatus.SUCCESS;
import static com.kanwise.report_service.model.notification.email.EmailMessageType.PERSONAL_REPORT;
import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static java.util.concurrent.CompletableFuture.completedFuture;

@Slf4j
@Service
public class PersonalReportJobExecutorService implements JobExecutorService<PersonalReportJobExecutionDetails, PersonalReportJobInformation> {

    private static final String DIRECTORY_PATTERN = "reports/%s/personal/";
    private static final String FILE_NAME_PATTERN = "%s-personal-report-%s.pdf";
    private final IEmailService emailService;
    private final ISpacesService spacesService;
    private final ReportService personalReportService;
    private final ReportDataService<PersonalReportJobInformation> personalReportDataService;
    private final String spaceName;

    public PersonalReportJobExecutorService(IEmailService emailService, ISpacesService spacesService, ReportService personalReportService, ReportDataService<PersonalReportJobInformation> personalReportDataService, SpacesNamesConfigurationProperties spacesNamesConfigurationProperties) {
        this.emailService = emailService;
        this.spacesService = spacesService;
        this.personalReportService = personalReportService;
        this.personalReportDataService = personalReportDataService;
        spaceName = spacesNamesConfigurationProperties.reports();
    }


    @Async
    @Override
    public CompletableFuture<PersonalReportJobExecutionDetails> execute(PersonalReportJobInformation jobInformation) {
        Map<String, Object> data = personalReportDataService.getReportData(jobInformation);
        String fileName = constructFileName(jobInformation);
        String directoryPath = DIRECTORY_PATTERN.formatted(jobInformation.getUsername());
        String reportUrl = constructReportUrl(fileName, directoryPath);

        MultipartFile generatedReport = generateReport(data, fileName);
        uploadFile(generatedReport, directoryPath).thenAccept(handleFileUploadResult(data, fileName, reportUrl));

        return completedFuture(constructJobExecutionDetails(jobInformation, reportUrl));
    }

    private String constructReportUrl(String fileName, String directoryPath) {
        return checkIfDirectoryExists(spaceName, directoryPath) + fileName;
    }

    private Consumer<FileUploadStatus> handleFileUploadResult(Map<String, Object> data, String fileName, String reportUrl) {
        return uploadStatus -> {
            if (uploadStatus.isSuccessful()) {
                sendEmail(reportUrl, data);
                try {
                    personalReportService.removeReport(fileName);
                } catch (IOException e) {
                    log.error("Failed to remove report file: {}", e.getMessage());
                }
            }
        };
    }

    private MultipartFile generateReport(Map<String, Object> data, String fileName) {
        return personalReportService.generateReport(data, fileName);
    }

    private URL checkIfDirectoryExists(String spaceName, String formatted) {
        return spacesService.checkIfDirectoryExists(spaceName, formatted, true);
    }

    private CompletableFuture<FileUploadStatus> uploadFile(MultipartFile report, String formatted) {
        return spacesService.uploadFile(report, formatted, spaceName);
    }

    private void sendEmail(String reportUrl, Map<String, Object> data) {
        emailService.sendEmail(constructEmailRequest(reportUrl, data));
    }

    private PersonalReportJobExecutionDetails constructJobExecutionDetails(PersonalReportJobInformation jobInformation, String spacesUrl) {
        return PersonalReportJobExecutionDetails.builder()
                .jobInformation(jobInformation)
                .message(JOB_EXECUTION_SUCCESS_MESSAGE)
                .executionStatus(SUCCESS)
                .reportUrl(spacesUrl)
                .build();
    }

    private String constructFileName(PersonalReportJobInformation jobInformationService) {
        return String.format(FILE_NAME_PATTERN, jobInformationService.getUsername(), now());
    }

    private EmailRequest constructEmailRequest(String reportUrl, Map<String, Object> data) {
        return EmailRequest.builder()
                .to(data.get("email").toString())
                .subject("Personal Report")
                .isHtml(true)
                .data(of("firstName", data.get("firstName").toString(),
                        "reportStartDate", now(),
                        "reportEndDate", now(),
                        "reportType", "personal",
                        "href", reportUrl))
                .type(PERSONAL_REPORT)
                .build();
    }
}
