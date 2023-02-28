package com.kanwise.kanwise_service.model.member_statistics.constaraint;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Getter
@Setter
public class MemberStatisticsConstraints {
    private LocalDateTime startDate = now().minusMonths(1);
    private LocalDateTime endDate = now();
}
