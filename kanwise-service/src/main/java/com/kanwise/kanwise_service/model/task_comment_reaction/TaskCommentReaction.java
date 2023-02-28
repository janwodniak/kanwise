package com.kanwise.kanwise_service.model.task_comment_reaction;

import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.task_comment.TaskComment;
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
import javax.persistence.Table;
import java.time.LocalDateTime;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "taskcommentreaction", schema = "public")
public class TaskCommentReaction {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false)
    private Long id;
    @ManyToOne(cascade = {MERGE})
    private Member author;
    @ManyToOne(cascade = {MERGE})
    private TaskComment comment;
    private LocalDateTime reactedAt;
    @Enumerated(STRING)
    private ReactionLabel reactionLabel;

    public void setComment(TaskComment comment) {
        this.comment = comment;
        comment.getReactions().add(this);
    }
}
