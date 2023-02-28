package com.kanwise.kanwise_service.service.membership.implementation;

import com.kanwise.kanwise_service.model.membership.Membership;
import com.kanwise.kanwise_service.repository.member.MembershipRepository;
import com.kanwise.kanwise_service.service.membership.IMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MembershipService implements IMembershipService {

    private final MembershipRepository membershipRepository;

    @Override
    public Set<Membership> findMembershipsByProjectAndUsernames(long projectId, Set<String> usernames) {
        return membershipRepository.findAllByProjectIdAndMemberUsernameIn(projectId, usernames);
    }

    @Override
    public Set<Membership> findMembershipsByProject(long projectId) {
        return membershipRepository.findAllByProjectId(projectId);
    }
}
