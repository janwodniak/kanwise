package com.kanwise.kanwise_service.repository.member;

import com.kanwise.kanwise_service.model.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsActiveByUsername(String username);

    @Modifying
    @Query("UPDATE Member m SET m.active = FALSE WHERE m.username = ?1")
    void deleteByUsername(String username);

    Optional<Member> findMemberByUsername(String username);

    Optional<Member> findActiveByUsername(String username);

    Set<Member> findByUsernameIn(Set<String> usernames);

    boolean existsByUsername(String username);
}
