package com.kanwise.report_service.model.monitoring.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.util.Map;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;

@MappedSuperclass
@Getter
@Setter
@RequiredArgsConstructor
@SuperBuilder
public abstract class JobLog {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false)
    private Long id;
    private String message;
    private LocalDateTime timestamp;
    private String level;
    @ElementCollection(fetch = EAGER)
    private Map<String, String> data;
}
