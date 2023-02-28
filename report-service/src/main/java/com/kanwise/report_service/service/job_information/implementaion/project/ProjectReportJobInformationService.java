package com.kanwise.report_service.service.job_information.implementaion.project;

import com.kanwise.report_service.error.job.common.JobNotFoundException;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.repository.job.common.JobRepository;
import com.kanwise.report_service.service.job_information.common.JobInformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class ProjectReportJobInformationService implements JobInformationService<ProjectReportJobInformation> {

    private final JobRepository<ProjectReportJobInformation> jobRepository;

    @Override
    public ProjectReportJobInformation saveJobInformation(ProjectReportJobInformation jobInformation) {
        return jobRepository.save(jobInformation);
    }

    @Transactional(readOnly = true)
    @Override
    public ProjectReportJobInformation getJobInformation(String id) {
        return jobRepository.findById(id).orElseThrow(() -> new JobNotFoundException(id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProjectReportJobInformation> getAllJobInformation() {
        return jobRepository.findAll();
    }

    @Override
    public void deleteJobInformation(String id) {
        jobRepository.deleteById(id);
    }

    @Override
    public ProjectReportJobInformation updateJobInformation(ProjectReportJobInformation jobInformation) {
        return jobRepository.save(jobInformation);
    }
}
