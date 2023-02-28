package com.kanwise.user_service.model.user;

import com.kanwise.user_service.model.authentication.two_factor_authentication.TwoFactorAction;
import com.kanwise.user_service.model.image.Image;
import com.kanwise.user_service.model.notification.subscribtions.UserNotificationType;
import com.kanwise.user_service.model.otp.OneTimePassword;
import com.kanwise.user_service.model.security.UserRole;
import com.kanwise.user_service.model.token.PasswordResetToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.kanwise.user_service.model.image.ImageRole.PROFILE_IMAGE;
import static com.kanwise.user_service.model.image.ImageRole.UNSIGNED_IMAGE;
import static java.lang.Boolean.TRUE;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "user", schema = "public")
@Where(clause = "active = true")
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false)
    private Long id;
    private String password;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phoneNumber;
    private boolean twoFactorEnabled;
    private boolean active = TRUE;
    private boolean isEnabled;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    @Enumerated(STRING)
    private UserRole userRole;
    @ElementCollection(fetch = EAGER)
    @MapKeyEnumerated(STRING)
    private Map<UserNotificationType, Boolean> notificationSubscriptions;
    @ElementCollection(fetch = EAGER)
    @MapKeyEnumerated(STRING)
    private Map<TwoFactorAction, Boolean> twoFactorSubscriptions;
    private LocalDateTime lastLoginDate;
    private LocalDateTime joinDate;
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = {MERGE}, fetch = EAGER)
    private Set<Image> images = new HashSet<>();
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = {MERGE})
    private Set<PasswordResetToken> tokens = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = {MERGE})
    private Set<OneTimePassword> oneTimePasswords = new HashSet<>();

    public void addPasswordResetToken(PasswordResetToken resetToken) {
        this.tokens.add(resetToken);
        resetToken.setUser(this);
    }

    public void addOneTimePassword(OneTimePassword oneTimePassword) {
        this.oneTimePasswords.add(oneTimePassword);
        oneTimePassword.setUser(this);
    }

    public void addImage(Image image) {
        this.images.add(image);
        image.setUser(this);
    }

    public String getProfileImageUrl() {
        return this.images.stream()
                .filter(image -> image.getImageRole() == PROFILE_IMAGE)
                .sorted(Comparator.comparing(Image::getUploadedAt).reversed())
                .map(Image::getImageUrl)
                .findFirst()
                .orElse(null);
    }

    public void setProfileImage(Image image) {
        this.images.stream()
                .filter(i -> i.getImageRole().equals(PROFILE_IMAGE))
                .forEach(i -> i.setImageRole(UNSIGNED_IMAGE));
        image.setImageRole(PROFILE_IMAGE);
    }

    public boolean isSubscribedTo(UserNotificationType userNotificationType) {
        return this.notificationSubscriptions.get(userNotificationType);
    }
}
