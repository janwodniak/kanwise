package com.kanwise.kanwise_service.model.task;

import com.kanwise.kanwise_service.error.custom.project.MemberNotAssignedToProjectException;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.membership.Membership;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.task_comment.TaskComment;
import com.kanwise.kanwise_service.model.task_status.TaskStatus;
import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static java.time.Duration.ZERO;
import static java.util.Comparator.comparing;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.FetchType.EAGER;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Where(clause = "active = true")
@Table(name = "task", schema = "public")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private long id;
    private String title;
    private String description;
    private Duration estimatedTime;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private TaskStatusLabel currentStatus;
    @ManyToOne(cascade = {MERGE})
    private Membership author;
    @Enumerated(EnumType.STRING)
    private TaskPriority priority;
    @Enumerated(EnumType.STRING)
    private TaskType type;
    private boolean active;
    @ManyToOne(cascade = {MERGE})
    private Project project;
    @Builder.Default
    @ManyToMany(cascade = {MERGE}, fetch = EAGER)
    private Set<Membership> assignedMemberships = new HashSet<>();
    @Builder.Default
    @OneToMany(cascade = {MERGE}, fetch = EAGER, mappedBy = "task")
    private Set<TaskComment> comments = new HashSet<>();
    @Builder.Default
    @OneToMany(cascade = {PERSIST, MERGE}, fetch = EAGER, mappedBy = "task")
    private Set<TaskStatus> statuses = new HashSet<>();

    public void setAuthor(Member author) {
        author.getMembershipByProject(project).ifPresentOrElse(
                membership -> {
                    this.author = membership;
                    membership.getAssignedTasks().add(this);
                },
                () -> {
                    throw new MemberNotAssignedToProjectException(author.getUsername(), this.project.getId());
                });
    }

    public void setProject(Project project) {
        this.project = project;
        project.getTasks().add(this);
    }

    public void addMembers(Set<Member> members) {
        members.forEach(this::addMember);
    }

    public void addMember(Member member) {
        member.getMembershipByProject(this.project).ifPresentOrElse(
                membership -> {
                    this.assignedMemberships.add(membership);
                    membership.getAssignedTasks().add(this);
                },
                () -> {
                    throw new MemberNotAssignedToProjectException(member.getUsername(), this.project.getId());
                });
    }

    public void addStatus(TaskStatus taskStatus) {
        this.statuses.add(taskStatus);
        taskStatus.setTask(this);
    }

    public void updatePenultimateStatusSetTill(LocalDateTime time) {
        this.statuses.stream()
                .sorted(comparing(TaskStatus::getSetAt).reversed())
                .skip(1)
                .findFirst()
                .ifPresent(taskStatus -> taskStatus.setSetTill(time));
    }

    public Duration getSummaryTimeForTaskStatusLabel(TaskStatusLabel taskStatusLabel, Clock clock) {
        return statuses.stream()
                .filter(taskStatus -> taskStatus.getLabel().equals(taskStatusLabel))
                .map(taskStatus -> taskStatus.getActualDuration(clock))
                .reduce(ZERO, Duration::plus);
    }

    public void removeTask() {
        this.active = false;
    }
}
