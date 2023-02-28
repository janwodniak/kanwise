package com.kanwise.kanwise_service.service.statistics.member;

import com.kanwise.kanwise_service.model.member_statistics.MemberStatistics;
import com.kanwise.kanwise_service.model.member_statistics.constaraint.MemberStatisticsConstraints;

public interface IMemberStatisticsService {
    MemberStatistics findStatisticsForMember(String username, MemberStatisticsConstraints constraints);
}
