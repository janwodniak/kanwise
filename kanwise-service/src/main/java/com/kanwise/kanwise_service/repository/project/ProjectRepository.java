package com.kanwise.kanwise_service.repository.project;

import com.kanwise.kanwise_service.model.project.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByTitleContaining(String title, Pageable pageable);

    boolean existsByIdAndMembershipsMemberUsername(long id, String username);
}

