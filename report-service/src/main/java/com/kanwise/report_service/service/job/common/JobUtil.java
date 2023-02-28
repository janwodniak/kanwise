package com.kanwise.report_service.service.job.common;

import com.kanwise.report_service.model.job_information.common.JobInformation;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;

import static com.kanwise.report_service.constant.job.JobConstant.ID;
import static java.lang.System.currentTimeMillis;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;


public interface JobUtil<T extends JobInformation> {

    default JobDetail buildJobDetail(Class<? extends Job> jobClass, T jobInformation, String group) {
        String id = jobInformation.getId();
        JobDataMap newJobDataMap = new JobDataMap();
        newJobDataMap.put(ID, jobInformation.getId());

        return JobBuilder
                .newJob(jobClass)
                .withIdentity(id, group)
                .usingJobData(newJobDataMap)
                .requestRecovery(true)
                .build();
    }

    default Trigger buildTrigger(T jobInformation) {
        ScheduleBuilder<? extends Trigger> scheduleBuilder = jobInformation.isCronBased() ? cronSchedule(jobInformation.getCron()) : getSimpleScheduleBuilder(jobInformation);

        return TriggerBuilder
                .newTrigger()
                .withIdentity(jobInformation.getId())
                .withSchedule(scheduleBuilder)
                .startAt(new Date(currentTimeMillis() + jobInformation.getInitialOffsetMs()))
                .build();
    }

    default SimpleScheduleBuilder getSimpleScheduleBuilder(JobInformation jobInformation) {
        SimpleScheduleBuilder simpleScheduleBuilder = simpleSchedule().withIntervalInMilliseconds(jobInformation.getRepeatInterval());
        if (jobInformation.isRunForever()) {
            simpleScheduleBuilder.repeatForever();
        } else {
            simpleScheduleBuilder.withRepeatCount(jobInformation.getTotalFireCount() - 1);
        }
        return simpleScheduleBuilder;
    }
}
