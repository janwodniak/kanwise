package com.kanwise.kanwise_service.model.join.response;

import com.kanwise.kanwise_service.error.custom.join.request.JoinRequestAlreadyRespondedException;
import com.kanwise.kanwise_service.model.join.request.JoinRequest;
import com.kanwise.kanwise_service.model.join.request.JoinRequestStatus;
import com.kanwise.kanwise_service.model.member.Member;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "joinResponse", schema = "public")
public class JoinResponse {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false)
    private long id;
    @ManyToOne(cascade = {MERGE})
    private Member respondedBy;
    @OneToOne(cascade = {MERGE}, fetch = EAGER)
    private JoinRequest joinRequest;
    @Enumerated(STRING)
    private JoinRequestStatus status;
    private String message;
    private LocalDateTime respondedAt;

    public void setJoinRequest(JoinRequest joinRequest) {
        if (joinRequest.isResponded()) {
            throw new JoinRequestAlreadyRespondedException();
        } else {
            this.joinRequest = joinRequest;
            this.joinRequest.setJoinResponse(this);
        }
    }
}
