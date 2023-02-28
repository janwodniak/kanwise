package com.kanwise.kanwise_service.service.join.request.implementation;

import com.kanwise.kanwise_service.error.custom.join.request.JoinRequestNotFoundException;
import com.kanwise.kanwise_service.error.custom.member.MemberAlreadyAssignedToProjectException;
import com.kanwise.kanwise_service.model.join.request.JoinRequest;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.repository.join.request.JoinRequestRepository;
import com.kanwise.kanwise_service.service.join.request.IJoinRequestService;
import com.kanwise.kanwise_service.service.project.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class JoinRequestService implements IJoinRequestService {

    private final JoinRequestRepository joinRequestRepository;

    private final IProjectService projectService;

    @Transactional
    @Override
    public JoinRequest saveJoinRequest(JoinRequest joinRequest) {
        validateJoinRequest(joinRequest.getRequestedBy(), joinRequest.getProject());
        return joinRequestRepository.saveAndFlush(joinRequest);
    }

    @Override
    public JoinRequest findJoinRequestById(long id) {
        return joinRequestRepository.findById(id).orElseThrow(JoinRequestNotFoundException::new);
    }

    @Override
    public Set<JoinRequest> findJoinRequestsForProject(long id, boolean responded) {
        return projectService.findProjectJoinRequests(id)
                .stream()
                .filter(joinRequest -> responded == joinRequest.isResponded())
                .collect(toSet());
    }

    private void validateJoinRequest(Member requestedBy, Project project) {
        if (projectService.isProjectMember(project, requestedBy)) {
            throw new MemberAlreadyAssignedToProjectException(requestedBy.getUsername(), project.getId());
        }
    }
}
