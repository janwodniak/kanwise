package com.kanwise.report_service.service.scheduler.implementation;

import com.kanwise.report_service.error.job.common.JobAlreadyExistsException;
import com.kanwise.report_service.error.job.common.JobNotFoundException;
import com.kanwise.report_service.error.job.common.JobPausingException;
import com.kanwise.report_service.error.job.common.JobResumingException;
import com.kanwise.report_service.error.job_trigger_listener.JobTriggerListenerRegistrationException;
import com.kanwise.report_service.listener.common.GenericJobTriggerListener;
import com.kanwise.report_service.model.job_information.common.JobInformation;
import com.kanwise.report_service.service.job.common.JobUtil;
import com.kanwise.report_service.service.job_information.common.JobInformationService;
import com.kanwise.report_service.service.scheduler.common.JobSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.kanwise.report_service.constant.job.JobConstant.ID;
import static com.kanwise.report_service.model.report.JobGroup.findByJobInformationClass;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.quartz.JobKey.jobKey;
import static org.quartz.impl.matchers.GroupMatcher.groupEquals;
import static org.quartz.impl.matchers.GroupMatcher.groupStartsWith;
import static org.springframework.core.GenericTypeResolver.resolveTypeArgument;
import static org.springframework.core.ResolvableType.forClassWithGenerics;

@Slf4j
public abstract class GenericJobSchedulerService<T extends JobInformation> implements JobSchedulerService<T>, JobUtil<T> {

    private final Scheduler scheduler;
    private final ApplicationContext applicationContext;
    private final Class<T> genericType;
    private final JobInformationService<T> jobInformationService;

    @SuppressWarnings("unchecked")
    protected GenericJobSchedulerService(Scheduler scheduler, ApplicationContext applicationContext, JobInformationService<T> jobInformationService) {
        this.scheduler = scheduler;
        this.applicationContext = applicationContext;
        this.jobInformationService = jobInformationService;
        this.genericType = (Class<T>) resolveTypeArgument(getClass(), GenericJobSchedulerService.class);
    }


    @PostConstruct
    public void init() {
        CompletableFuture.supplyAsync(this::startScheduler).thenAccept(started -> {
            log.info("Scheduler started: {}", started);
            if (ofNullable(started).orElse(false)) {
                retrieveAndRegisterTriggerListener();
            }
        });
    }

    @PreDestroy
    public void preDestroy() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public T schedule(Class<? extends Job> jobClass, T jobInformation, String group) {
        try {
            JobDetail jobDetail = buildJobDetail(jobClass, jobInformation, group);
            Trigger trigger = buildTrigger(jobInformation);
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (ObjectAlreadyExistsException e) {
            throw new JobAlreadyExistsException(jobInformation.getId());
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
        return jobInformation;
    }

    @Override
    public List<T> getAllRunningJobsInGroup(String group) {
        try {
            return scheduler.getJobKeys(groupStartsWith(group)).stream()
                    .map(this::getJobInformation)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            return emptyList();
        }
    }

    @Override
    public T getRunningJob(String name, String group) {
        return getJobInformation(jobKey(name, group));
    }

    @Override
    public void deleteJob(String id, String group) {
        try {
            if (scheduler.checkExists(new JobKey(id, group))) {
                scheduler.deleteJob(new JobKey(id, group));
                jobInformationService.deleteJobInformation(id);
            } else {
                log.warn("Job with id {} and group {} does not exist", id, group);
                throw new JobNotFoundException(id);
            }
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public T pauseJob(T jobInformation, String group) {
        try {
            scheduler.pauseJob(jobKey(jobInformation.getId(), group));
        } catch (SchedulerException e) {
            log.error("Failed to pause job {}", jobInformation.getId(), e);
            throw new JobPausingException(jobInformation.getId());
        }
        return jobInformation;
    }

    @Override
    public T resumeJob(T jobInformation, String group) {
        try {
            scheduler.resumeJob(jobKey(jobInformation.getId(), group));
        } catch (SchedulerException e) {
            log.error("Failed to resume job {}", jobInformation.getId(), e);
            throw new JobResumingException(jobInformation.getId());
        }
        return jobInformation;
    }

    @SuppressWarnings("unchecked")
    private void retrieveAndRegisterTriggerListener() {
        getGenericBeanProvider(GenericJobTriggerListener.class, genericType)
                .stream()
                .findFirst()
                .map(listener -> (GenericJobTriggerListener<T>) listener)
                .ifPresentOrElse(this::registerListener, this::handleListenerNotFound);
    }

    private boolean startScheduler() {
        try {
            if (!scheduler.isStarted()) {
                scheduler.start();
            }
            return true;
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private void registerListener(GenericJobTriggerListener<T> genericListener) {
        String groupName = getGroupName();
        try {
            registerListenerForGroup(genericListener, groupName);
        } catch (SchedulerException e) {
            handleListenerRegistrationException(groupName, e);
        }
    }

    private void handleListenerNotFound() {
        String groupName = getGroupName();
        log.warn("No listener found for group {}", groupName);
        throw new JobTriggerListenerRegistrationException(groupName);
    }

    private String getGroupName() {
        return findByJobInformationClass(genericType).name();
    }

    private void registerListenerForGroup(GenericJobTriggerListener<T> genericListener, String groupName) throws SchedulerException {
        log.info("Registering listener for group {}", groupName);
        scheduler.getListenerManager().addTriggerListener(genericListener, groupEquals(groupName));
    }

    @SuppressWarnings("SameParameterValue")
    private ObjectProvider<Object> getGenericBeanProvider(Class<?> beanClass, Class<T> genericClass) {
        return applicationContext.getBeanProvider(forClassWithGenerics(beanClass, genericClass));
    }

    @SuppressWarnings("unchecked")
    private T getJobInformation(JobKey jobKey) {
        try {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            String id = (String) jobDetail.getJobDataMap().get(ID);
            return jobInformationService.getJobInformation(id);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private void handleListenerRegistrationException(String groupName, SchedulerException e) {
        log.error("Failed to register listener for group {}", groupName, e);
        throw new JobTriggerListenerRegistrationException(groupName);
    }
}
