package com.kanwise.user_service.model.image;

import com.kanwise.user_service.model.file.FileUploadStatus;
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
import java.time.LocalDateTime;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "profileImage", schema = "public")
public class Image {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private LocalDateTime uploadedAt;
    private String imageUrl;
    private String imageName;
    @Enumerated(STRING)
    private ImageRole imageRole;
    @Enumerated(STRING)
    private FileUploadStatus uploadStatus;

    @Builder
    public Image(User user, LocalDateTime uploadedAt, String imageUrl, String imageName, FileUploadStatus uploadStatus, ImageRole imageRole) {
        this.uploadedAt = uploadedAt;
        this.imageUrl = imageUrl;
        this.imageName = imageName;
        this.uploadStatus = uploadStatus;
        this.imageRole = imageRole;
        user.addImage(this);
    }
}
