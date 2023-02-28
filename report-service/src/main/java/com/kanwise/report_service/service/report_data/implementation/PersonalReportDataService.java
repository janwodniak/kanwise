package com.kanwise.report_service.service.report_data.implementation;

import com.kanwise.clients.report_service.report.client.ReportDataClient;
import com.kanwise.clients.report_service.report.model.personal.PersonalReportDataDto;
import com.kanwise.clients.report_service.report.model.personal.PersonalReportDataRequest;
import com.kanwise.clients.user_service.user.client.UserClient;
import com.kanwise.clients.user_service.user.model.UserDataDto;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.service.report_data.common.ReportDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
@Service
public class PersonalReportDataService implements ReportDataService<PersonalReportJobInformation> {

    private final UserClient userClient;
    private final ReportDataClient reportDataClient;


    @Override
    public Map<String, Object> getReportData(PersonalReportJobInformation jobInformation) {
        PersonalReportDataDto personalReportData = getPersonalReportData(jobInformation);
        UserDataDto userData = getUserData(jobInformation);
        return constructData(personalReportData.data(), userData.data());
    }

    private UserDataDto getUserData(PersonalReportJobInformation jobInformation) {
        ResponseEntity<UserDataDto> userData = userClient.getUserData(jobInformation.getUsername());
        return requireNonNull(userData.getBody());
    }

    private PersonalReportDataDto getPersonalReportData(PersonalReportJobInformation jobInformation) {
        PersonalReportDataRequest request = PersonalReportDataRequest.builder()
                .username(jobInformation.getUsername())
                .startDate(jobInformation.getStartDate())
                .endDate(jobInformation.getEndDate())
                .build();

        ResponseEntity<PersonalReportDataDto> personalReportData = reportDataClient.getPersonalReportData(request);
        return requireNonNull(personalReportData.getBody());
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
