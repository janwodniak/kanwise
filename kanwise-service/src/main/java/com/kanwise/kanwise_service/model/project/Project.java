package com.kanwise.kanwise_service.model.project;

import com.kanwise.kanwise_service.model.join.request.JoinRequest;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.membership.Membership;
import com.kanwise.kanwise_service.model.task.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.kanwise.kanwise_service.model.membership.MembershipStatus.ACTIVE;
import static com.kanwise.kanwise_service.model.project.ProjectStatus.DELETED;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toSet;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Where(clause = "active = true")
@Table(name = "project", schema = "public")
public class Project {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false)
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private boolean active;
    @Enumerated(STRING)
    private ProjectStatus status;
    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = {PERSIST, MERGE}, fetch = EAGER)
    private Set<Membership> memberships = new HashSet<>();
    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = {MERGE}, fetch = EAGER)
    private Set<Task> tasks = new HashSet<>();
    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = {MERGE}, fetch = EAGER)
    private Set<JoinRequest> joinRequests = new HashSet<>();


    public void addMembers(Set<Member> members) {
        members.forEach(this::addMember);
    }

    public void addMember(Member member) {
        Membership membership = Membership.builder()
                .status(ACTIVE)
                .updatedAt(now())
                .member(member)
                .assignedTasks(new HashSet<>())
                .taskComments(new HashSet<>())
                .project(this)
                .active(true)
                .build();
        memberships.add(membership);
        member.getMemberships().add(membership);
    }

    public Set<Member> getMembers() {
        return memberships.stream()
                .map(Membership::getMember)
                .collect(toSet());
    }

    public void removeProject() {
        this.active = false;
        this.status = DELETED;
        this.tasks.forEach(Task::removeTask);
        this.memberships.forEach(Membership::deleteMembership);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        Project project = (Project) o;
        return id != null && Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
