package com.kanwise.report_service.model.subscriber;

import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Where(clause = "active = true")
@Table(name = "subscriber", schema = "public")
public class Subscriber {
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false)
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;
    @Builder.Default
    @OneToMany(mappedBy = "subscriber", fetch = FetchType.EAGER)
    private Set<PersonalReportJobInformation> personalReportJobInformation = new HashSet<>();
    @Builder.Default
    @OneToMany(mappedBy = "subscriber", fetch = FetchType.EAGER)
    private Set<ProjectReportJobInformation> projectReportJobInformation = new HashSet<>();
    private boolean active;
}
