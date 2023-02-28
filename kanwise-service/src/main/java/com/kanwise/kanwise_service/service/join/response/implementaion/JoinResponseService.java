package com.kanwise.kanwise_service.service.join.response.implementaion;

import com.kanwise.kanwise_service.error.custom.join.response.JoinResponseNotFoundException;
import com.kanwise.kanwise_service.error.custom.member.MemberAlreadyAssignedToProjectException;
import com.kanwise.kanwise_service.error.custom.project.MemberNotAssignedToProjectException;
import com.kanwise.kanwise_service.model.join.response.JoinResponse;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.repository.join.response.JoinResponseRepository;
import com.kanwise.kanwise_service.service.join.response.IJoinResponseService;
import com.kanwise.kanwise_service.service.project.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.kanwise.kanwise_service.model.join.request.JoinRequestStatus.ACCEPTED;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class JoinResponseService implements IJoinResponseService {

    private final JoinResponseRepository joinResponseRepository;

    private final IProjectService projectService;


    @Transactional
    @Override
    public JoinResponse saveJoinResponse(JoinResponse joinResponse) {
        Project project = joinResponse.getJoinRequest().getProject();
        Member requestedBy = joinResponse.getJoinRequest().getRequestedBy();

        validateJoinResponse(joinResponse);

        if (joinResponse.getStatus() == ACCEPTED) {
            project.addMember(requestedBy);
        }

        return joinResponseRepository.saveAndFlush(joinResponse);
    }

    @Override
    public JoinResponse findJoinResponseById(long id) {
        return joinResponseRepository.findById(id).orElseThrow(JoinResponseNotFoundException::new);
    }

    @Override
    public Set<JoinResponse> findJoinResponsesForProject(long id) {
        return projectService.findProjectJoinResponses(id);
    }

    private void validateJoinResponse(JoinResponse joinResponse) {
        Project project = joinResponse.getJoinRequest().getProject();
        Member requestedBy = joinResponse.getJoinRequest().getRequestedBy();
        Member respondedBy = joinResponse.getRespondedBy();

        if (!projectService.isProjectMember(project, respondedBy)) {
            throw new MemberNotAssignedToProjectException(respondedBy.getUsername(), project.getId());
        }

        if (projectService.isProjectMember(project, requestedBy)) {
            throw new MemberAlreadyAssignedToProjectException(requestedBy.getUsername(), project.getId());
        }
    }
}
