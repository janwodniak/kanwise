package com.kanwise.kanwise_service.service.join.response;

import com.kanwise.kanwise_service.model.join.response.JoinResponse;

import java.util.Set;


public interface IJoinResponseService {
    JoinResponse saveJoinResponse(JoinResponse joinResponse);

    JoinResponse findJoinResponseById(long id);

    Set<JoinResponse> findJoinResponsesForProject(long id);
}
