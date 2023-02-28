package com.kanwise.report_service.service.subscriber.common;

import com.kanwise.report_service.model.job_information.common.JobStatus;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.model.subscriber.Subscriber;
import com.kanwise.report_service.model.subscriber.command.EditSubscriberCommand;
import com.kanwise.report_service.model.subscriber.command.EditSubscriberPartiallyCommand;

import java.util.List;
import java.util.Set;

public interface ISubscriberService {
    Subscriber createSubscriber(Subscriber subscriber);

    Subscriber getSubscriber(String username);

    List<Subscriber> getAllSubscribers();

    void deleteSubscriber(String username);

    Subscriber editSubscriber(String username, EditSubscriberCommand command);

    Subscriber editSubscriberPartially(String username, EditSubscriberPartiallyCommand command);

    Set<ProjectReportJobInformation> getProjectReports(String username, JobStatus status);

    Set<ProjectReportJobInformation> getProjectReports(String username);

    Set<PersonalReportJobInformation> getPersonalReports(String username, JobStatus status);

    Set<PersonalReportJobInformation> getPersonalReports(String username);
}
