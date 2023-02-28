package com.kanwise.kanwise_service.service.project;

import com.kanwise.kanwise_service.model.join.request.JoinRequest;
import com.kanwise.kanwise_service.model.join.response.JoinResponse;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.membership.Membership;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.project.command.EditProjectCommand;
import com.kanwise.kanwise_service.model.project.command.EditProjectPartiallyCommand;
import com.kanwise.kanwise_service.model.task.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface IProjectService {
    Project findProjectById(long id);

    Page<Project> findProjects(String title, Pageable pageable);

    Project saveProject(Project map);

    void deleteProject(long id);

    Project editProject(long id, EditProjectCommand command);

    Project editProjectPartially(long id, EditProjectPartiallyCommand command);

    Set<Member> findProjectMembers(long id);

    Set<Task> findProjectTasks(long id);

    Set<Task> findProjectTasksForMember(long id, String username);

    Set<Member> assignMembersToTask(long projectId, long taskId, Set<String> usernames);

    Set<Member> assignMembersToProject(long projectId, Set<String> usernames);

    void discardMemberFromProject(long id, String username);

    boolean isProjectMember(Project project, Member member);

    Set<Membership> findMembershipsForProject(long id);

    Set<Membership> findMembershipsForProjectByUsernames(long id, Set<String> usernames);

    Set<JoinRequest> findProjectJoinRequests(long id);

    Set<JoinResponse> findProjectJoinResponses(long id);
}
