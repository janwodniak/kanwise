package com.kanwise.kanwise_service.model.report.project.mapping;

import com.kanwise.clients.report_service.report.model.project.ProjectReportDataDto;
import com.kanwise.kanwise_service.model.report.project.ProjectReportData;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

@Service
public class ProjectReportDataToProjectReportDataDtoConverter implements Converter<ProjectReportData, ProjectReportDataDto> {
    @Override
    public ProjectReportDataDto convert(MappingContext<ProjectReportData, ProjectReportDataDto> mappingContext) {
        return null;
    }
}
