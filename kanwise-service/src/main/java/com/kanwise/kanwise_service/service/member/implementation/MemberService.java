package com.kanwise.kanwise_service.service.member.implementation;

import com.kanwise.kanwise_service.error.custom.member.MemberNotFoundException;
import com.kanwise.kanwise_service.model.join.request.JoinRequest;
import com.kanwise.kanwise_service.model.join.response.JoinResponse;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.member.command.EditMemberCommand;
import com.kanwise.kanwise_service.model.member.command.EditMemberPartiallyCommand;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_comment.TaskComment;
import com.kanwise.kanwise_service.repository.member.MemberRepository;
import com.kanwise.kanwise_service.service.member.IMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MemberService implements IMemberService {

    private final MemberRepository memberRepository;

    @Override
    public Member findMemberByUsername(String username) {
        return memberRepository.findMemberByUsername(username).orElseThrow(MemberNotFoundException::new);
    }

    @Override
    public Optional<Member> findMember(String username) {
        return memberRepository.findMemberByUsername(username);
    }

    @Override
    public Set<Project> findProjectsForMember(String username) {
        return memberRepository.findMemberByUsername(username)
                .map(Member::getProjects)
                .orElseThrow(MemberNotFoundException::new);
    }

    @Override
    public Set<Task> findTasksForMember(String username) {
        return memberRepository.findActiveByUsername(username)
                .map(Member::getAssignedTasks).orElseThrow(MemberNotFoundException::new);
    }

    @Override
    public Set<TaskComment> findCommentsForMember(String username) {
        return memberRepository.findActiveByUsername(username)
                .map(Member::getTaskComments).orElseThrow(MemberNotFoundException::new);
    }

    @Override
    public Set<JoinRequest> findJoinRequestsForMember(String username) {
        return memberRepository.findActiveByUsername(username)
                .map(Member::getJoinRequests).orElseThrow(MemberNotFoundException::new);
    }

    @Override
    public Set<JoinResponse> findJoinResponsesForMember(String username) {
        return memberRepository.findActiveByUsername(username)
                .map(Member::getJoinResponses).orElseThrow(MemberNotFoundException::new);
    }

    @Override
    public Set<Member> findMembersByUsernames(Set<String> usernames) {
        return memberRepository.findByUsernameIn(usernames);
    }

    @Override
    public boolean existsByUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    @Transactional
    @Override
    public Member saveMember(Member member) {
        return memberRepository.saveAndFlush(member);
    }

    @Transactional
    @Override
    public void deleteMember(String username) {
        if (memberRepository.existsActiveByUsername(username)) {
            memberRepository.deleteByUsername(username);
        } else {
            throw new MemberNotFoundException();
        }
    }

    @Transactional
    @Override
    public Member editMember(String username, EditMemberCommand command) {
        return memberRepository.findActiveByUsername(username).map(memberToEdit -> {
            memberToEdit.setUsername(command.username());
            memberToEdit.setNotificationSubscriptions(command.notificationSubscriptions());
            return memberToEdit;
        }).orElseThrow(MemberNotFoundException::new);
    }

    @Transactional
    @Override
    public Member editMemberPartially(String username, EditMemberPartiallyCommand command) {
        return memberRepository.findActiveByUsername(username).map(memberToEdit -> {
            ofNullable(command.username()).ifPresent(memberToEdit::setUsername);
            ofNullable(command.notificationSubscriptions()).ifPresent(memberToEdit::updateNotificationSubscriptions);
            return memberToEdit;
        }).orElseThrow(MemberNotFoundException::new);
    }
}
