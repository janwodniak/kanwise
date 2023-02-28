package com.kanwise.report_service.model.job_information.common;

import com.kanwise.report_service.model.subscriber.Subscriber;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

import static javax.persistence.CascadeType.MERGE;

@MappedSuperclass
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@SuperBuilder
public class JobInformation {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    @ManyToOne(cascade = MERGE)
    @JoinColumn(name = "subscriber_id")
    private Subscriber subscriber;
    private String jobType;
    private int totalFireCount;
    private int remainingFireCount;
    private boolean runForever;
    private long repeatInterval;
    private long initialOffsetMs;
    private String cron;
    private LocalDateTime createdAt;
    private JobStatus status;
    private boolean active;

    public boolean isCronBased() {
        return this.cron != null && !this.cron.isEmpty();
    }

    public boolean isFireCountBased() {
        return !this.isRunForever() || !this.isCronBased();
    }

    public void decreaseRemainingFireCount() {
        --this.remainingFireCount;
    }
}
