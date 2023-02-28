package com.kanwise.report_service.service.subscriber.implementation;

import com.kanwise.report_service.error.subscriber.SubscriberNotFoundException;
import com.kanwise.report_service.model.job_information.common.JobStatus;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.model.subscriber.Subscriber;
import com.kanwise.report_service.model.subscriber.command.EditSubscriberCommand;
import com.kanwise.report_service.model.subscriber.command.EditSubscriberPartiallyCommand;
import com.kanwise.report_service.repository.subscriber.SubscriberRepository;
import com.kanwise.report_service.service.subscriber.common.ISubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Transactional
@RequiredArgsConstructor
@Service
public class SubscriberService implements ISubscriberService {

    private final SubscriberRepository subscriberRepository;

    @Override
    public Subscriber createSubscriber(Subscriber subscriber) {
        return subscriberRepository.saveAndFlush(subscriber);
    }

    @Transactional(readOnly = true)
    @Override
    public Subscriber getSubscriber(String username) {
        return subscriberRepository.findByUsername(username).orElseThrow(() -> new SubscriberNotFoundException(username));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Subscriber> getAllSubscribers() {
        return subscriberRepository.findAll();
    }

    @Override
    public void deleteSubscriber(String username) {
        if (subscriberRepository.existsByUsername(username)) {
            subscriberRepository.deleteByUsername(username);
        } else {
            throw new SubscriberNotFoundException(username);
        }
    }

    @Override
    public Subscriber editSubscriber(String username, EditSubscriberCommand command) {
        return subscriberRepository.findByUsername(username).map(subscriber -> {
            subscriber.setUsername(command.username());
            subscriber.setEmail(command.email());
            return subscriber;
        }).orElseThrow(() -> new SubscriberNotFoundException(username));
    }

    @Override
    public Subscriber editSubscriberPartially(String username, EditSubscriberPartiallyCommand command) {
        return subscriberRepository.findByUsername(username).map(subscriber -> {
            ofNullable(command.username()).ifPresent(subscriber::setUsername);
            ofNullable(command.email()).ifPresent(subscriber::setEmail);
            return subscriber;
        }).orElseThrow(() -> new SubscriberNotFoundException(username));


    }

    @Override
    public Set<ProjectReportJobInformation> getProjectReports(String username, JobStatus status) {
        return subscriberRepository.findByUsername(username)
                .map(subscriber -> subscriber.getProjectReportJobInformation().stream()
                        .filter(jobInformation -> jobInformation.getStatus().equals(status))
                        .collect(Collectors.toSet()))
                .orElseThrow(() -> new SubscriberNotFoundException(username));
    }

    @Override
    public Set<ProjectReportJobInformation> getProjectReports(String username) {
        return subscriberRepository.findByUsername(username)
                .map(Subscriber::getProjectReportJobInformation)
                .orElseThrow(() -> new SubscriberNotFoundException(username));
    }

    @Override
    public Set<PersonalReportJobInformation> getPersonalReports(String username, JobStatus status) {
        return subscriberRepository.findByUsername(username)
                .map(subscriber -> subscriber.getPersonalReportJobInformation().stream()
                        .filter(jobInformation -> jobInformation.getStatus().equals(status))
                        .collect(Collectors.toSet()))
                .orElseThrow(() -> new SubscriberNotFoundException(username));
    }

    @Override
    public Set<PersonalReportJobInformation> getPersonalReports(String username) {
        return subscriberRepository.findByUsername(username)
                .map(Subscriber::getPersonalReportJobInformation)
                .orElseThrow(() -> new SubscriberNotFoundException(username));
    }
}
