package com.kanwise.kanwise_service.service.member;

import com.kanwise.kanwise_service.model.join.request.JoinRequest;
import com.kanwise.kanwise_service.model.join.response.JoinResponse;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.member.command.EditMemberCommand;
import com.kanwise.kanwise_service.model.member.command.EditMemberPartiallyCommand;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_comment.TaskComment;

import java.util.Optional;
import java.util.Set;


public interface IMemberService {

    Member saveMember(Member member);

    void deleteMember(String username);

    Member editMember(String username, EditMemberCommand command);

    Member editMemberPartially(String username, EditMemberPartiallyCommand command);

    Optional<Member> findMember(String username);

    Member findMemberByUsername(String username);

    Set<Project> findProjectsForMember(String username);


    Set<Task> findTasksForMember(String username);

    Set<TaskComment> findCommentsForMember(String username);

    Set<JoinRequest> findJoinRequestsForMember(String username);

    Set<JoinResponse> findJoinResponsesForMember(String username);

    Set<Member> findMembersByUsernames(Set<String> usernames);

    boolean existsByUsername(String username);
}
