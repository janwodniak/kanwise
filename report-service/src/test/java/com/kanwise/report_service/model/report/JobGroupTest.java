package com.kanwise.report_service.model.report;

import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobGroupTest {

    @Test
    void findByJobInformationClass() {
        // Given
        // When
        JobGroup personalReportJobGroup = JobGroup.findByJobInformationClass(PersonalReportJobInformation.class);
        JobGroup projectReportJobGroup = JobGroup.findByJobInformationClass(ProjectReportJobInformation.class);
        // Then
        assertEquals(JobGroup.PERSONAL_REPORT, personalReportJobGroup);
        assertEquals(JobGroup.PROJECT_REPORT, projectReportJobGroup);
    }
}