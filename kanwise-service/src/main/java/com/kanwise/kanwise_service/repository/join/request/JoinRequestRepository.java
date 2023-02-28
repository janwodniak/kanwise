package com.kanwise.kanwise_service.repository.join.request;

import com.kanwise.kanwise_service.model.join.request.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    Set<JoinRequest> findAllByProjectId(long id);
}
