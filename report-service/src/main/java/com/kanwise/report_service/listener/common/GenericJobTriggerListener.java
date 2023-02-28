package com.kanwise.report_service.listener.common;

import com.kanwise.report_service.job.common.JobIdentityResolver;
import com.kanwise.report_service.model.job_information.common.JobInformation;
import com.kanwise.report_service.service.job_information.common.JobInformationService;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

@RequiredArgsConstructor
public abstract class GenericJobTriggerListener<T extends JobInformation> implements TriggerListener, JobIdentityResolver {

    private final JobInformationService<T> jobInformationService;

    @Override
    public String getName() {
        return GenericJobTriggerListener.class.getSimpleName();
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext jobExecutionContext) {
        T jobInformation = getJobInfo(jobExecutionContext);
        if (jobInformation.isFireCountBased() && jobInformation.getRemainingFireCount() > 0) {
            jobInformation.decreaseRemainingFireCount();
            jobInformationService.updateJobInformation(jobInformation);
        }
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext jobExecutionContext) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext jobExecutionContext, Trigger.CompletedExecutionInstruction completedExecutionInstruction) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private T getJobInfo(JobExecutionContext jobExecutionContext) {
        return jobInformationService.getJobInformation(resolveJobId(jobExecutionContext));
    }
}
