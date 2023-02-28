package com.kanwise.user_service.repository.user;

import com.kanwise.user_service.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Modifying
    @Query("UPDATE User u SET u.active = FALSE WHERE u.id = ?1")
    void softDeleteById(long id);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsActiveById(long id);

    Optional<User> findActiveUserById(long id);

    Optional<User> findUserByUsername(String username);

    Optional<User> findUserByEmail(String username);

    Set<User> findByUsernameIn(List<String> usernames);

    Page<User> findByLastNameContaining(String lastName, Pageable pageable);

    boolean existsByPhoneNumber(String phoneNumber);
}
