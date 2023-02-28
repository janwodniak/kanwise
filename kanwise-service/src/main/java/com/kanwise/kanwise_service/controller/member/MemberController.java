package com.kanwise.kanwise_service.controller.member;

import com.kanwise.kanwise_service.error.handling.ExceptionHandling;
import com.kanwise.kanwise_service.model.join.request.dto.JoinRequestDto;
import com.kanwise.kanwise_service.model.join.response.dto.JoinResponseDto;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.member.command.CreateMemberCommand;
import com.kanwise.kanwise_service.model.member.command.EditMemberCommand;
import com.kanwise.kanwise_service.model.member.command.EditMemberPartiallyCommand;
import com.kanwise.kanwise_service.model.member.dto.MemberDto;
import com.kanwise.kanwise_service.model.member_statistics.MemberStatistics;
import com.kanwise.kanwise_service.model.member_statistics.constaraint.MemberStatisticsConstraints;
import com.kanwise.kanwise_service.model.member_statistics.dto.MemberStatisticsDto;
import com.kanwise.kanwise_service.model.project.dto.ProjectDto;
import com.kanwise.kanwise_service.model.task.dto.TaskDto;
import com.kanwise.kanwise_service.model.task_comment.dto.TaskCommentDto;
import com.kanwise.kanwise_service.service.member.IMemberService;
import com.kanwise.kanwise_service.service.statistics.member.IMemberStatisticsService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

import static com.kanwise.kanwise_service.model.http.HttpMethod.PATCH;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RequestMapping("/member")
@RestController
public class MemberController extends ExceptionHandling {

    private final IMemberService memberService;
    private final IMemberStatisticsService memberStatisticService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Create member",
            notes = "This endpoint is used to create member.",
            response = MemberDto.class,
            responseReference = "ResponseEntity<MemberDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping
    public ResponseEntity<MemberDto> createMember(@RequestBody @Valid CreateMemberCommand command) {
        Member member = memberService.saveMember(modelMapper.map(command, Member.class));
        return new ResponseEntity<>(modelMapper.map(member, MemberDto.class), CREATED);
    }

    @ApiOperation(value = "Get member by username",
            notes = "This endpoint is used to get member by username.",
            response = MemberDto.class,
            responseReference = "ResponseEntity<MemberDto>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('MEMBER_READ')")
    @GetMapping("/{username}")
    public ResponseEntity<MemberDto> findMemberByUsername(@PathVariable("username") String username) {
        Member member = memberService.findMemberByUsername(username);
        return new ResponseEntity<>(modelMapper.map(member, MemberDto.class), OK);
    }

    @ApiOperation(value = "Delete a member",
            notes = "This endpoint is used to delete a member.",
            response = HttpStatus.class,
            responseReference = "ResponseEntity<HttpStatus>",
            httpMethod = DELETE,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@memberAuthenticationFacade.isMemberByUsernameAndHasAuthority(#username, 'MEMBER_DELETE') or @memberAuthenticationFacade.admin")
    @DeleteMapping("/{username}")
    public ResponseEntity<HttpStatus> deleteMember(@PathVariable("username") String username) {
        memberService.deleteMember(username);
        return new ResponseEntity<>(NO_CONTENT);
    }

    @ApiOperation(value = "Edit a member",
            notes = "This endpoint is used to edit a member.",
            response = MemberDto.class,
            responseReference = "ResponseEntity<MemberDto>",
            httpMethod = PUT,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@memberAuthenticationFacade.isMemberByUsernameAndHasAuthority(#username, 'MEMBER_WRITE') or @memberAuthenticationFacade.admin")
    @PutMapping("/{username}")
    public ResponseEntity<MemberDto> editMember(@PathVariable("username") String username, @RequestBody @Valid EditMemberCommand command) {
        Member member = memberService.editMember(username, command);
        return new ResponseEntity<>(modelMapper.map(member, MemberDto.class), OK);
    }

    @ApiOperation(value = "Edit a member partially",
            notes = "This endpoint is used to edit a member partially.",
            response = MemberDto.class,
            responseReference = "ResponseEntity<MemberDto>",
            httpMethod = PATCH,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@memberAuthenticationFacade.isMemberByUsernameAndHasAuthority(#username, 'MEMBER_WRITE') or @memberAuthenticationFacade.admin")
    @PatchMapping("/{username}")
    public ResponseEntity<MemberDto> editMemberPartially(@PathVariable("username") String username, @RequestBody @Valid EditMemberPartiallyCommand command) {
        Member member = memberService.editMemberPartially(username, command);
        return new ResponseEntity<>(modelMapper.map(member, MemberDto.class), OK);
    }

    @ApiOperation(value = "Get member projects",
            notes = "This endpoint is used to get member projects.",
            response = ProjectDto.class,
            responseReference = "ResponseEntity<Set<ProjectDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@memberAuthenticationFacade.isMemberByUsernameAndHasAuthority(#username, 'MEMBER_READ') or @memberAuthenticationFacade.admin")
    @GetMapping("/{username}/projects")
    public ResponseEntity<Set<ProjectDto>> findProjectsForMember(@PathVariable("username") String username) {
        return new ResponseEntity<>(memberService.findProjectsForMember(username).stream().map(project -> modelMapper.map(project, ProjectDto.class)).collect(toSet()), OK);
    }

    @ApiOperation(value = "Get member tasks",
            notes = "This endpoint is used to get member tasks.",
            response = TaskDto.class,
            responseReference = "ResponseEntity<Set<TaskDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@memberAuthenticationFacade.isMemberByUsernameAndHasAuthority(#username, 'MEMBER_READ') or @memberAuthenticationFacade.admin")
    @GetMapping("/{username}/tasks")
    public ResponseEntity<Set<TaskDto>> findTasksForMember(@PathVariable("username") String username) {
        return new ResponseEntity<>(memberService.findTasksForMember(username).stream().map(task -> modelMapper.map(task, TaskDto.class)).collect(toSet()), OK);
    }

    @ApiOperation(value = "Get member task comments",
            notes = "This endpoint is used to get member task comments.",
            response = TaskCommentDto.class,
            responseReference = "ResponseEntity<Set<CommentDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@memberAuthenticationFacade.isMemberByUsernameAndHasAuthority(#username, 'MEMBER_READ') or @memberAuthenticationFacade.admin")
    @GetMapping("/{username}/comments")
    public ResponseEntity<Set<TaskCommentDto>> findTaskCommentsForMember(@PathVariable("username") String username) {
        return new ResponseEntity<>(memberService.findCommentsForMember(username).stream().map(task -> modelMapper.map(task, TaskCommentDto.class)).collect(toSet()), OK);
    }

    @ApiOperation(value = "Get member statistics",
            notes = "This endpoint is used to get member statistics.",
            response = MemberStatisticsDto.class,
            responseReference = "ResponseEntity<MemberStatisticsDto>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@memberAuthenticationFacade.isMemberByUsernameAndHasAuthority(#username, 'MEMBER_READ') or @memberAuthenticationFacade.admin")
    @GetMapping("/{username}/statistics")
    public ResponseEntity<MemberStatisticsDto> findStatisticsForMember(@PathVariable("username") String username, @Valid MemberStatisticsConstraints constraints) {
        MemberStatistics memberStatistics = memberStatisticService.findStatisticsForMember(username, constraints);
        return new ResponseEntity<>(modelMapper.map(memberStatistics, MemberStatisticsDto.class), OK);
    }

    @ApiOperation(value = "Get member join requests",
            notes = "This endpoint is used to get member join requests.",
            response = JoinRequestDto.class,
            responseReference = "ResponseEntity<Set<JoinRequestDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@memberAuthenticationFacade.isMemberByUsernameAndHasAuthority(#username, 'MEMBER_READ') or @memberAuthenticationFacade.admin")
    @GetMapping("/{username}/join/requests")
    public ResponseEntity<Set<JoinRequestDto>> findJoinRequestsForMember(@PathVariable("username") String username) {
        return new ResponseEntity<>(memberService.findJoinRequestsForMember(username).stream().map(joinRequest -> modelMapper.map(joinRequest, JoinRequestDto.class)).collect(toSet()), OK);
    }

    @ApiOperation(value = "Get member join responses",
            notes = "This endpoint is used to get member join responses.",
            response = JoinResponseDto.class,
            responseReference = "ResponseEntity<Set<JoinResponseDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@memberAuthenticationFacade.isMemberByUsernameAndHasAuthority(#username, 'MEMBER_READ') or @memberAuthenticationFacade.admin")
    @GetMapping("/{username}/join/responses")
    public ResponseEntity<Set<JoinResponseDto>> findJoinResponsesForMember(@PathVariable("username") String username) {
        return new ResponseEntity<>(memberService.findJoinResponsesForMember(username).stream().map(joinResponse -> modelMapper.map(joinResponse, JoinResponseDto.class)).collect(toSet()), OK);
    }
}
