package com.kanwise.report_service.service.report_data.implementation;

import com.kanwise.clients.report_service.report.client.ReportDataClient;
import com.kanwise.clients.report_service.report.model.project.ProjectReportDataDto;
import com.kanwise.clients.report_service.report.model.project.ProjectReportDataRequest;
import com.kanwise.clients.user_service.user.client.UserClient;
import com.kanwise.clients.user_service.user.model.UserDataDto;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.service.report_data.common.ReportDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
@Service
public class ProjectReportDataService implements ReportDataService<ProjectReportJobInformation> {

    private final UserClient userClient;

    private final ReportDataClient reportDataClient;


    @Override
    public Map<String, Object> getReportData(ProjectReportJobInformation jobInformation) {
        ProjectReportDataDto projectReportData = getProjectReportData(jobInformation);
        UserDataDto userData = getUserData(jobInformation);
        return constructData(projectReportData.data(), userData.data());
    }

    private UserDataDto getUserData(ProjectReportJobInformation jobInformation) {
        ResponseEntity<UserDataDto> userData = userClient.getUserData(jobInformation.getUsername());
        return requireNonNull(userData.getBody());
    }

    private ProjectReportDataDto getProjectReportData(ProjectReportJobInformation jobInformation) {
        ProjectReportDataRequest request = ProjectReportDataRequest.builder()
                .projectId(jobInformation.getProjectId())
                .startDate(jobInformation.getStartDate())
                .endDate(jobInformation.getEndDate())
                .build();

        ResponseEntity<ProjectReportDataDto> projectReportData = reportDataClient.getProjectReportData(request);
        return requireNonNull(projectReportData.getBody());
    }

    @SafeVarargs
    private Map<String, Object> constructData(Map<String, Object>... data) {
        Map<String, Object> result = new HashMap<>();
        for (Map<String, Object> item : data) {
            result.putAll(item);
        }
        return result;
    }
}
