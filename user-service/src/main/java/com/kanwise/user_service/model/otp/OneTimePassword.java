package com.kanwise.user_service.model.otp;

import com.kanwise.clients.user_service.authentication.model.OtpStatus;
import com.kanwise.user_service.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Clock;
import java.time.LocalDateTime;

import static com.kanwise.clients.user_service.authentication.model.OtpStatus.CONFIRMED;
import static com.kanwise.clients.user_service.authentication.model.OtpStatus.DELIVERED;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "oneTimePassword", schema = "public")
public class OneTimePassword {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;
    @Column(nullable = false)
    private String code;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;
    @Enumerated(STRING)
    private OtpStatus status;

    @ManyToOne(cascade = MERGE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public boolean isConfirmed() {
        return this.confirmedAt != null && this.status == CONFIRMED;
    }

    public boolean isExpired(Clock clock) {
        return this.expiresAt.isBefore(LocalDateTime.now(clock));
    }

    public boolean isDelivered() {
        return this.status == DELIVERED;
    }
}
