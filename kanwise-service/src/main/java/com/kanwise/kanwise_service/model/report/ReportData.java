package com.kanwise.kanwise_service.model.report;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder
@Getter
@Setter
public abstract class ReportData {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
