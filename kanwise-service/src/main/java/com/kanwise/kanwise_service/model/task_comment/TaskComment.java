package com.kanwise.kanwise_service.model.task_comment;

import com.kanwise.kanwise_service.error.custom.project.MemberNotAssignedToProjectException;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.membership.Membership;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_comment_reaction.ReactionLabel;
import com.kanwise.kanwise_service.model.task_comment_reaction.TaskCommentReaction;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.kanwise.kanwise_service.model.task_comment_reaction.ReactionLabel.DISLIKE;
import static com.kanwise.kanwise_service.model.task_comment_reaction.ReactionLabel.LIKE;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "taskcomment", schema = "public")
public class TaskComment {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false)
    private Long id;
    @ManyToOne(cascade = {MERGE})
    private Membership author;
    @ManyToOne(cascade = {MERGE})
    private Task task;
    private String content;
    private LocalDateTime commentedAt;
    @Builder.Default
    @OneToMany(mappedBy = "comment", fetch = EAGER)
    private Set<TaskCommentReaction> reactions = new HashSet<>();

    public void setAuthor(Member author) {
        author.getMembershipByProject(this.task.getProject()).ifPresentOrElse(
                membership -> {
                    this.author = membership;
                    membership.getTaskComments().add(this);
                },
                () -> {
                    throw new MemberNotAssignedToProjectException(author.getUsername(), this.task.getProject().getId());
                }
        );
    }

    public void setTask(Task task) {
        this.task = task;
        task.getComments().add(this);
    }

    public long getLikesCount() {
        return getReactionCount(LIKE);
    }

    public long getDislikesCount() {
        return getReactionCount(DISLIKE);
    }

    private long getReactionCount(ReactionLabel reactionLabel) {
        return reactions.stream().filter(reaction -> reaction.getReactionLabel().equals(reactionLabel)).count();
    }
}
