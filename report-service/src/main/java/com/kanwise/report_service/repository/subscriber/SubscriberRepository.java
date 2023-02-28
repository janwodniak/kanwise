package com.kanwise.report_service.repository.subscriber;

import com.kanwise.report_service.model.subscriber.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, String> {
    Optional<Subscriber> findByUsername(String usernames);

    boolean existsByUsername(String username);

    @Modifying
    @Query("UPDATE Subscriber s SET s.active = FALSE WHERE s.username = ?1")
    void deleteByUsername(String username);
}
