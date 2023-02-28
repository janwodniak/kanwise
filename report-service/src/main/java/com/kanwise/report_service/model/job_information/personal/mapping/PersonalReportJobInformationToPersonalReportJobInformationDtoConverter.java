package com.kanwise.report_service.model.job_information.personal.mapping;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.kanwise.report_service.controller.job.personal.monitoring.PersonalReportJobMonitoringController;
import com.kanwise.report_service.controller.subscriber.SubscriberController;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.job_information.personal.dto.PersonalReportJobInformationDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static com.cronutils.model.CronType.QUARTZ;
import static java.util.Locale.ENGLISH;
import static java.util.Optional.empty;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class PersonalReportJobInformationToPersonalReportJobInformationDtoConverter implements Converter<PersonalReportJobInformation, PersonalReportJobInformationDto> {

    @Override
    public PersonalReportJobInformationDto convert(MappingContext<PersonalReportJobInformation, PersonalReportJobInformationDto> mappingContext) {
        PersonalReportJobInformation personalReportJobInformation = mappingContext.getSource();
        PersonalReportJobInformationDto personalReportJobInformationDto = generatePersonalReportJobInformationDto(personalReportJobInformation);
        addHateoasLinks(personalReportJobInformation, personalReportJobInformationDto);
        return personalReportJobInformationDto;
    }

    private void addHateoasLinks(PersonalReportJobInformation personalReportJobInformation, PersonalReportJobInformationDto personalReportJobInformationDto) {
        personalReportJobInformationDto.add(linkTo(methodOn(SubscriberController.class).getSubscriber(personalReportJobInformation.getSubscriber().getUsername())).withRel("subscriber"));
        personalReportJobInformationDto.add(linkTo(methodOn(PersonalReportJobMonitoringController.class).getJobLogs(personalReportJobInformation.getId())).withRel("job-logs"));
        personalReportJobInformationDto.add(linkTo(methodOn(SubscriberController.class).getPersonalReports(personalReportJobInformation.getSubscriber().getUsername(), empty())).withRel("personal-reports"));
        personalReportJobInformationDto.add(linkTo(methodOn(SubscriberController.class).getProjectReports(personalReportJobInformation.getSubscriber().getUsername(), empty())).withRel("project-reports"));
    }

    private PersonalReportJobInformationDto generatePersonalReportJobInformationDto(PersonalReportJobInformation personalReportJobInformation) {
        if (personalReportJobInformation.isCronBased()) {
            return generateCronJobInformationDto(personalReportJobInformation);
        } else if (personalReportJobInformation.isRunForever()) {
            return generateRunForeverJobInformationDto(personalReportJobInformation);
        } else {
            return generateFireCountJobInformationDto(personalReportJobInformation);
        }
    }

    private PersonalReportJobInformationDto generateFireCountJobInformationDto(PersonalReportJobInformation jobInfo) {
        return generateBasePersonalJobReportInformationDtoBuilder(jobInfo)
                .totalFireCount(jobInfo.getTotalFireCount())
                .remainingFireCount(jobInfo.getRemainingFireCount())
                .initialOffsetMs(jobInfo.getInitialOffsetMs())
                .repeatInterval(jobInfo.getRepeatInterval())
                .runForever(jobInfo.isRunForever())
                .build();
    }

    private PersonalReportJobInformationDto generateRunForeverJobInformationDto(PersonalReportJobInformation jobInfo) {
        return generateBasePersonalJobReportInformationDtoBuilder(jobInfo)
                .runForever(jobInfo.isRunForever())
                .initialOffsetMs(jobInfo.getInitialOffsetMs())
                .repeatInterval(jobInfo.getRepeatInterval())
                .build();
    }

    private PersonalReportJobInformationDto generateCronJobInformationDto(PersonalReportJobInformation jobInfo) {
        return generateBasePersonalJobReportInformationDtoBuilder(jobInfo)
                .cron(jobInfo.getCron())
                .description(generateCronDescription(jobInfo.getCron()))
                .build();
    }

    private PersonalReportJobInformationDto.PersonalReportJobInformationDtoBuilder generateBasePersonalJobReportInformationDtoBuilder(PersonalReportJobInformation jobInfo) {
        return PersonalReportJobInformationDto.builder()
                .id(jobInfo.getId())
                .subscriberUsername(jobInfo.getUsername())
                .startDate(jobInfo.getStartDate())
                .endDate(jobInfo.getEndDate())
                .status(jobInfo.getStatus());
    }

    private String generateCronDescription(String cron) {
        CronDescriptor descriptor = CronDescriptor.instance(ENGLISH);
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        return descriptor.describe(parser.parse(cron));
    }
}
