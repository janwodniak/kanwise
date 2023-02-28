package com.kanwise.kanwise_service.repository.member;

import com.kanwise.kanwise_service.model.membership.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Set<Membership> findAllByProjectIdAndMemberUsernameIn(long id, Collection<String> usernames);

    Set<Membership> findAllByProjectId(long projectId);
}
