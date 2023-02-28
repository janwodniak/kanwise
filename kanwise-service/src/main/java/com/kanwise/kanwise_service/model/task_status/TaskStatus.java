package com.kanwise.kanwise_service.model.task_status;

import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.task.Task;
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
import javax.persistence.Table;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static java.util.Objects.requireNonNullElseGet;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "taskstatus", schema = "public")
public class TaskStatus {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false)
    private long id;
    private TaskStatusLabel label;
    private LocalDateTime setAt;
    private LocalDateTime setTill;

    @ManyToOne(cascade = {MERGE})
    private Member setBy;
    @ManyToOne(cascade = {MERGE})
    private Task task;

    public void setTask(Task task) {
        this.task = task;
        task.getStatuses().add(this);
        task.setCurrentStatus(this.label);
    }

    public Duration getActualDuration(Clock clock) {
        return between(setAt, requireNonNullElseGet(setTill, () -> now(clock)));
    }

    public boolean isOngoing() {
        return setTill == null;
    }
}
