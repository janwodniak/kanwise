package com.kanwise.report_service.model.report;

import com.kanwise.report_service.model.job_information.common.JobInformation;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum JobGroup {
    PERSONAL_REPORT(PersonalReportJobInformation.class),
    PROJECT_REPORT(ProjectReportJobInformation.class);

    private static final Map<Class<? extends JobInformation>, JobGroup> map;

    static {
        map = new HashMap<>();
        for (JobGroup v : JobGroup.values()) {
            map.put(v.jobInformationClass, v);
        }
    }

    private final Class<? extends JobInformation> jobInformationClass;

    public static JobGroup findByJobInformationClass(Class<? extends JobInformation> clazz) {
        return map.get(clazz);
    }
}
