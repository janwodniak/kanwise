package com.kanwise.kanwise_service.model.join.request;

import com.kanwise.kanwise_service.model.join.response.JoinResponse;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.project.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "joinRequest", schema = "public")
public class JoinRequest {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false)
    private long id;
    @ManyToOne(cascade = {MERGE})
    private Project project;
    @ManyToOne(cascade = {MERGE})
    private Member requestedBy;
    @OneToOne(cascade = {MERGE})
    private JoinResponse joinResponse;
    private LocalDateTime requestedAt;
    private String message;

    public void setRequestedBy(Member requestedBy) {
        this.requestedBy = requestedBy;
        this.requestedBy.getJoinRequests().add(this);
    }

    public void setProject(Project project) {
        this.project = project;
        this.project.getJoinRequests().add(this);
    }

    public boolean isResponded() {
        return joinResponse != null;
    }
}
