package com.kanwise.kanwise_service.service.project.implementation;

import com.kanwise.kanwise_service.error.custom.member.MemberAlreadyAssignedToProjectException;
import com.kanwise.kanwise_service.error.custom.project.MemberNotAssignedToProjectException;
import com.kanwise.kanwise_service.error.custom.project.ProjectNotFoundException;
import com.kanwise.kanwise_service.error.custom.task.MemberAlreadyAssignedToTaskException;
import com.kanwise.kanwise_service.model.join.request.JoinRequest;
import com.kanwise.kanwise_service.model.join.response.JoinResponse;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.membership.Membership;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.project.command.EditProjectCommand;
import com.kanwise.kanwise_service.model.project.command.EditProjectPartiallyCommand;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.repository.project.ProjectRepository;
import com.kanwise.kanwise_service.service.member.IMemberService;
import com.kanwise.kanwise_service.service.project.IProjectService;
import com.kanwise.kanwise_service.service.task.ITaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

import static com.kanwise.kanwise_service.model.project.ProjectStatus.valueOf;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProjectService implements IProjectService {

    private final ProjectRepository projectRepository;
    private final IMemberService memberService;
    private final ITaskService taskService;

    @Override
    public Project findProjectById(long id) {
        return projectRepository.findById(id).orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public Page<Project> findProjects(String title, Pageable pageable) {
        return projectRepository.findByTitleContaining(title, pageable);
    }

    @Transactional
    @Override
    public Project saveProject(Project project) {
        return projectRepository.saveAndFlush(project);
    }

    @Transactional
    @Override
    public void deleteProject(long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
        } else {
            throw new ProjectNotFoundException();
        }
    }

    @Transactional
    @Override
    public Project editProject(long id, EditProjectCommand command) {
        return projectRepository.findById(id).map(projectToEdit -> {
            projectToEdit.setTitle(command.title());
            projectToEdit.setDescription(command.description());
            projectToEdit.setStatus(valueOf(command.status().toUpperCase()));
            return projectToEdit;
        }).orElseThrow(ProjectNotFoundException::new);
    }

    @Transactional
    @Override
    public Project editProjectPartially(long id, EditProjectPartiallyCommand command) {
        return projectRepository.findById(id).map(projectToEdit -> {
            ofNullable(command.title()).ifPresent(projectToEdit::setTitle);
            ofNullable(command.description()).ifPresent(projectToEdit::setDescription);
            ofNullable(command.status()).ifPresent(status -> projectToEdit.setStatus(valueOf(status.toUpperCase())));
            return projectToEdit;
        }).orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public Set<Task> findProjectTasks(long id) {
        return projectRepository.findById(id).map(Project::getTasks).orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public Set<Member> findProjectMembers(long id) {
        return projectRepository.findById(id).map(Project::getMembers).orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public Set<Task> findProjectTasksForMember(long id, String username) {
        return projectRepository.findById(id).map(project -> getTasksForMemberInProject(username, project)
        ).orElseThrow(ProjectNotFoundException::new);
    }

    @Transactional
    @Override
    public Set<Member> assignMembersToTask(long projectId, long taskId, Set<String> usernames) {
        return projectRepository.findById(projectId)
                .map(project -> assignMembers(taskId, usernames))
                .orElseThrow(ProjectNotFoundException::new);
    }

    private Set<Member> assignMembers(long taskId, Set<String> usernames) {
        Task task = taskService.findTaskById(taskId);
        Set<Member> members = usernames.stream().map(memberService::findMemberByUsername).collect(toSet());
        validateMembersToAssign(members, task);
        members.forEach(task::addMember);
        return members;
    }

    @Transactional
    @Override
    public void discardMemberFromProject(long id, String username) {
        projectRepository.findById(id).ifPresentOrElse(
                project -> deleteMembership(username, project),
                () -> {
                    throw new ProjectNotFoundException();
                }
        );
    }

    @Override
    public boolean isProjectMember(Project project, Member member) {
        return projectRepository.existsByIdAndMembershipsMemberUsername(project.getId(), member.getUsername());
    }

    @Override
    public Set<Membership> findMembershipsForProject(long id) {
        return projectRepository.findById(id)
                .map(Project::getMemberships)
                .orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public Set<Membership> findMembershipsForProjectByUsernames(long id, Set<String> usernames) {
        return projectRepository.findById(id)
                .map(project -> {
                    Set<Membership> memberships = project.getMemberships();
                    usernames.forEach(username -> {
                        if (memberships.stream().noneMatch(membership -> membership.getMember().getUsername().equals(username))) {
                            throw new MemberNotAssignedToProjectException(username, project.getId());
                        }
                    });
                    return memberships.stream()
                            .filter(membership -> usernames.contains(membership.getMember().getUsername()))
                            .collect(toSet());
                })
                .orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public Set<JoinRequest> findProjectJoinRequests(long id) {
        return projectRepository.findById(id)
                .map(Project::getJoinRequests)
                .orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public Set<JoinResponse> findProjectJoinResponses(long id) {
        return projectRepository.findById(id)
                .map(project -> project.getJoinRequests().stream()
                        .map(JoinRequest::getJoinResponse)
                        .filter(Objects::nonNull)
                        .collect(toSet()))
                .orElseThrow(ProjectNotFoundException::new);
    }

    @Transactional
    @Override
    public Set<Member> assignMembersToProject(long projectId, Set<String> usernames) {
        return projectRepository.findById(projectId)
                .map(project -> assignMembers(usernames, project))
                .orElseThrow(ProjectNotFoundException::new);
    }

    private Set<Member> assignMembers(Set<String> usernames, Project project) {
        Set<Member> members = memberService.findMembersByUsernames(usernames);
        validateMembersToAdd(members, project);
        members.forEach(project::addMember);
        return members;
    }

    private void deleteMembership(String username, Project project) {
        memberService.findMemberByUsername(username)
                .getMembershipByProject(project).ifPresentOrElse(
                        Membership::deleteMembership,
                        () -> {
                            throw new MemberNotAssignedToProjectException(username, project.getId());
                        }
                );
    }

    private Set<Task> getTasksForMemberInProject(String username, Project project) {
        return memberService.findMemberByUsername(username)
                .getMembershipByProject(project)
                .map(membership -> getTasksForMember(membership, project))
                .orElseThrow(() -> new MemberNotAssignedToProjectException(username, project.getId()));
    }

    private void validateMembersToAssign(Set<Member> members, Task task) {
        Project project = task.getProject();
        members.forEach(member -> {
            if (!member.isProjectMember(project)) {
                throw new MemberNotAssignedToProjectException(member.getUsername(), project.getId());
            }

            if (member.isAssignedToTask(task)) {
                throw new MemberAlreadyAssignedToTaskException(member.getUsername(), task.getId());
            }
        });
    }

    private void validateMembersToAdd(Set<Member> members, Project project) {
        members.forEach(member -> {
            if (member.isProjectMember(project)) {
                throw new MemberAlreadyAssignedToProjectException(member.getUsername(), project.getId());
            }
        });
    }

    private Set<Task> getTasksForMember(Membership membership, Project project) {
        return project.getTasks()
                .stream()
                .filter(task -> task.getAssignedMemberships().contains(membership))
                .collect(toSet());
    }
}
