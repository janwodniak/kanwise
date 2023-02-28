package com.kanwise.user_service.repository.authentication;


import com.kanwise.user_service.model.token.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.confirmedAt = ?1 WHERE p.token = ?2")
    void updateConfirmedAt(LocalDateTime confirmedAt, String token);

    Optional<PasswordResetToken> findByToken(String token);
}
