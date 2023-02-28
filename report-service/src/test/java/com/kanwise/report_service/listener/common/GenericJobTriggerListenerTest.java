package com.kanwise.report_service.listener.common;

import com.kanwise.report_service.model.job_information.common.JobInformation;
import com.kanwise.report_service.service.job_information.common.JobInformationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;

import java.util.Map;
import java.util.stream.Stream;

import static com.kanwise.report_service.constant.job.JobConstant.ID;
import static java.util.stream.Stream.of;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.quartz.Trigger.CompletedExecutionInstruction.DELETE_TRIGGER;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class GenericJobTriggerListenerTest {


    private static Stream<Arguments> shouldNotDecreaseFireCountArguments() {
        return of(
                Arguments.of(false, 2),
                Arguments.of(false, 0),
                Arguments.of(true, 0)
        );
    }

    @Test
    void shouldDecreaseFireCount() {
        // Given
        JobInformationService<JobInformation> jobInformationService = mock(JobInformationService.class);
        JobInformation jobInformation = mock(JobInformation.class);
        GenericJobTriggerListener<JobInformation> genericJobTriggerListener = new GenericJobTriggerListener<>(jobInformationService) {
        };
        Trigger trigger = mock(Trigger.class);
        JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
        JobDetail jobDetail = mock(JobDetail.class);
        // When
        when(jobInformationService.getJobInformation(any())).thenReturn(jobInformation);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(new JobDataMap(Map.of(ID, "id")));

        when(jobInformation.isFireCountBased()).thenReturn(true);
        when(jobInformation.getRemainingFireCount()).thenReturn(2);

        genericJobTriggerListener.triggerFired(trigger, jobExecutionContext);
        // Then
        verify(jobInformation, times(1)).decreaseRemainingFireCount();
        verify(jobInformationService, times(1)).updateJobInformation(jobInformation);
    }

    @ParameterizedTest
    @MethodSource("shouldNotDecreaseFireCountArguments")
    void shouldNotDecreaseFireCount(boolean isFireCountBased, int remainingFireCount) {
        // Given
        JobInformationService<JobInformation> jobInformationService = mock(JobInformationService.class);
        JobInformation jobInformation = mock(JobInformation.class, withSettings().lenient());
        GenericJobTriggerListener<JobInformation> genericJobTriggerListener = new GenericJobTriggerListener<>(jobInformationService) {
        };
        Trigger trigger = mock(Trigger.class);
        JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
        JobDetail jobDetail = mock(JobDetail.class);
        // When
        when(jobInformationService.getJobInformation(any())).thenReturn(jobInformation);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(new JobDataMap(Map.of(ID, "id")));

        when(jobInformation.isFireCountBased()).thenReturn(isFireCountBased);
        when(jobInformation.getRemainingFireCount()).thenReturn(remainingFireCount);

        genericJobTriggerListener.triggerFired(trigger, jobExecutionContext);
        // Then
        verify(jobInformation, times(0)).decreaseRemainingFireCount();
        verify(jobInformationService, times(0)).updateJobInformation(jobInformation);
    }


    @Test
    void shouldThrowNotImplementedExceptionForVetoingJobExecution() {
        // Given
        JobInformationService<JobInformation> jobInformationService = mock(JobInformationService.class);
        GenericJobTriggerListener<JobInformation> genericJobTriggerListener = new GenericJobTriggerListener<>(jobInformationService) {
        };
        Trigger trigger = mock(Trigger.class);
        JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
        // When
        // Then
        assertThrows(UnsupportedOperationException.class, () -> genericJobTriggerListener.vetoJobExecution(trigger, jobExecutionContext));
    }

    @Test
    void shouldThrowNotImplementedExceptionForTriggerMisfired() {
        // Given
        JobInformationService<JobInformation> jobInformationService = mock(JobInformationService.class);
        GenericJobTriggerListener<JobInformation> genericJobTriggerListener = new GenericJobTriggerListener<>(jobInformationService) {
        };
        Trigger trigger = mock(Trigger.class);
        // When
        // Then
        assertThrows(UnsupportedOperationException.class, () -> genericJobTriggerListener.triggerMisfired(trigger));
    }

    @Test
    void shouldThrowNotImplementedExceptionForTriggerComplete() {
        // Given
        JobInformationService<JobInformation> jobInformationService = mock(JobInformationService.class);
        GenericJobTriggerListener<JobInformation> genericJobTriggerListener = new GenericJobTriggerListener<>(jobInformationService) {
        };
        Trigger trigger = mock(Trigger.class);
        JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
        // When
        // Then
        assertThrows(UnsupportedOperationException.class, () -> genericJobTriggerListener.triggerComplete(trigger, jobExecutionContext, DELETE_TRIGGER));
    }

}