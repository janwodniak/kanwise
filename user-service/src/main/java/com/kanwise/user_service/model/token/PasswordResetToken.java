package com.kanwise.user_service.model.token;

import com.kanwise.user_service.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.Clock;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;
    @Column(nullable = false)
    private String token;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;

    @ManyToOne(cascade = MERGE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public boolean isConfirmed() {
        return this.confirmedAt != null;
    }

    public boolean isExpired(Clock clock) {
        return this.expiresAt.isBefore(now(clock));
    }
}
