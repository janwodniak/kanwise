package com.kanwise.kanwise_service.model.member;

import com.kanwise.kanwise_service.model.join.request.JoinRequest;
import com.kanwise.kanwise_service.model.join.response.JoinResponse;
import com.kanwise.kanwise_service.model.membership.Membership;
import com.kanwise.kanwise_service.model.notification.ProjectNotificationType;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_comment.TaskComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toSet;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Where(clause = "active = true")
@Table(name = "members", schema = "public")
public class Member {
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false)
    @Id
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = {MERGE}, fetch = EAGER)
    private Set<Membership> memberships = new HashSet<>();
    @Builder.Default
    @OneToMany(mappedBy = "requestedBy", cascade = {MERGE}, fetch = EAGER)
    private Set<JoinRequest> joinRequests = new HashSet<>();
    @Builder.Default
    @OneToMany(mappedBy = "respondedBy", cascade = {MERGE}, fetch = EAGER)
    private Set<JoinResponse> joinResponse = new HashSet<>();
    @Builder.Default
    @ElementCollection(fetch = EAGER)
    @MapKeyEnumerated(STRING)
    private Map<ProjectNotificationType, Boolean> notificationSubscriptions = new EnumMap<>(ProjectNotificationType.class);
    private boolean active;

    public Set<Project> getProjects() {
        return memberships.stream()
                .map(Membership::getProject)
                .collect(toSet());
    }

    public Set<Task> getAssignedTasks() {
        return memberships.stream()
                .map(Membership::getAssignedTasks)
                .flatMap(Set::stream)
                .collect(toSet());
    }

    public Set<TaskComment> getTaskComments() {
        return memberships.stream()
                .map(Membership::getTaskComments)
                .flatMap(Set::stream)
                .collect(toSet());
    }

    public boolean isProjectMember(Project project) {
        return this.getProjects().stream().anyMatch(isEqual(project));
    }

    public boolean isAssignedToTask(Task task) {
        return this.getAssignedTasks().stream().anyMatch(isEqual(task));
    }

    public Optional<Membership> getMembershipByProject(Project project) {
        return this.getMemberships().stream()
                .filter(m -> Objects.equals(m.getProject(), project))
                .findAny();
    }

    public void updateNotificationSubscriptions(Map<ProjectNotificationType, Boolean> notificationSubscriptions) {
        this.notificationSubscriptions.putAll(notificationSubscriptions);
    }

    public Set<JoinResponse> getJoinResponses() {
        return joinRequests.stream()
                .map(JoinRequest::getJoinResponse)
                .filter(Objects::nonNull)
                .collect(toSet());
    }
}
