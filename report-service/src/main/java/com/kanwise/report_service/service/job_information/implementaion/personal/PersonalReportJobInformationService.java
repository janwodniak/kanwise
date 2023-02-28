package com.kanwise.report_service.service.job_information.implementaion.personal;

import com.kanwise.report_service.error.job.common.JobNotFoundException;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.repository.job.common.JobRepository;
import com.kanwise.report_service.service.job_information.common.JobInformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class PersonalReportJobInformationService implements JobInformationService<PersonalReportJobInformation> {

    private final JobRepository<PersonalReportJobInformation> jobRepository;

    @Override
    public PersonalReportJobInformation saveJobInformation(PersonalReportJobInformation jobInformation) {
        return jobRepository.save(jobInformation);
    }

    @Transactional(readOnly = true)
    @Override
    public PersonalReportJobInformation getJobInformation(String id) {
        return jobRepository.findById(id).orElseThrow(() -> new JobNotFoundException(id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<PersonalReportJobInformation> getAllJobInformation() {
        return jobRepository.findAll();
    }

    @Override
    public void deleteJobInformation(String id) {
        PersonalReportJobInformation jobInformation = getJobInformation(id);
        jobInformation.setActive(false);
    }

    @Override
    public PersonalReportJobInformation updateJobInformation(PersonalReportJobInformation jobInformation) {
        return jobRepository.save(jobInformation);
    }
}
