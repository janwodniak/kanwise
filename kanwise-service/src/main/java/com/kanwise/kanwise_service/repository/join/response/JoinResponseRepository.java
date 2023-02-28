package com.kanwise.kanwise_service.repository.join.response;

import com.kanwise.kanwise_service.model.join.response.JoinResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface JoinResponseRepository extends JpaRepository<JoinResponse, Long> {
    Set<JoinResponse> findAllByJoinRequestProjectId(long id);
}
