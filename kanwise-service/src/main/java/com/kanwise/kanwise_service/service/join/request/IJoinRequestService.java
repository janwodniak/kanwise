package com.kanwise.kanwise_service.service.join.request;

import com.kanwise.kanwise_service.model.join.request.JoinRequest;

import java.util.Set;

public interface IJoinRequestService {
    JoinRequest saveJoinRequest(JoinRequest joinRequest);

    JoinRequest findJoinRequestById(long id);

    Set<JoinRequest> findJoinRequestsForProject(long id, boolean verified);
}
