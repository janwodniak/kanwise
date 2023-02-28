package com.kanwise.kanwise_service.model.membership;

import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_comment.TaskComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.kanwise.kanwise_service.model.membership.MembershipStatus.DELETED;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.FetchType.EAGER;

@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Where(clause = "active = true")
@Table(name = "membership", schema = "public")
public class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private long id;
    @ManyToOne(cascade = {MERGE})
    private Member member;
    @ManyToOne(cascade = {PERSIST, MERGE})
    private Project project;
    private MembershipStatus status;
    private LocalDateTime updatedAt;
    @Builder.Default
    @ManyToMany(mappedBy = "assignedMemberships", cascade = {MERGE}, fetch = EAGER)
    private Set<Task> assignedTasks = new HashSet<>();
    @Builder.Default
    @OneToMany(mappedBy = "author", cascade = {MERGE}, fetch = EAGER)
    private Set<TaskComment> taskComments = new HashSet<>();
    private boolean active;

    public void deleteMembership() {
        this.status = DELETED;
        this.active = false;
    }

    public void assignTask(Task task) {
        this.assignedTasks.add(task);
        task.getAssignedMemberships().add(this);
    }
}
