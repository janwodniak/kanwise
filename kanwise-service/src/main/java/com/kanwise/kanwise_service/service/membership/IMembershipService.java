package com.kanwise.kanwise_service.service.membership;

import com.kanwise.kanwise_service.model.membership.Membership;

import java.util.Set;

public interface IMembershipService {

    Set<Membership> findMembershipsByProjectAndUsernames(long projectId, Set<String> usernames);

    Set<Membership> findMembershipsByProject(long projectId);
}
