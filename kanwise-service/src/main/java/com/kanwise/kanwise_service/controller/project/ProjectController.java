package com.kanwise.kanwise_service.controller.project;


import com.kanwise.kanwise_service.error.handling.ExceptionHandling;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.member.dto.MemberDto;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.project.command.CreateProjectCommand;
import com.kanwise.kanwise_service.model.project.command.CreateProjectPageCommand;
import com.kanwise.kanwise_service.model.project.command.EditProjectCommand;
import com.kanwise.kanwise_service.model.project.command.EditProjectPartiallyCommand;
import com.kanwise.kanwise_service.model.project.dto.ProjectDto;
import com.kanwise.kanwise_service.model.task.dto.TaskDto;
import com.kanwise.kanwise_service.service.project.IProjectService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;
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
@RequestMapping("/project")
@RestController
public class ProjectController extends ExceptionHandling {

    private final IProjectService projectService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Create project",
            notes = "This endpoint is used to create project.",
            response = ProjectDto.class,
            responseReference = "ResponseEntity<ProjectDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PostMapping
    public ResponseEntity<ProjectDto> createProject(@RequestBody @Valid CreateProjectCommand command) {
        Project project = projectService.saveProject(modelMapper.map(command, Project.class));
        return new ResponseEntity<>(modelMapper.map(project, ProjectDto.class), CREATED);
    }

    @ApiOperation(value = "Get project",
            notes = "This endpoint is used to get project by projectId.",
            response = ProjectDto.class,
            responseReference = "ResponseEntity<ProjectDto>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDto> findProjectById(@PathVariable("projectId") long projectId) {
        Project project = projectService.findProjectById(projectId);
        return new ResponseEntity<>(modelMapper.map(project, ProjectDto.class), OK);
    }

    @ApiOperation(value = "Get projects",
            notes = "This endpoint is used to get projects using pagination.",
            response = ProjectDto.class,
            responseReference = "ResponseEntity<Page<ProjectDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping
    public ResponseEntity<Page<ProjectDto>> findProjects(@RequestParam Optional<String> title, @Valid CreateProjectPageCommand command) {
        Page<Project> projects = projectService.findProjects(title.orElse(""), modelMapper.map(command, Pageable.class));
        return new ResponseEntity<>(projects.map(project -> modelMapper.map(project, ProjectDto.class)), OK);
    }

    @ApiOperation(value = "Delete a project",
            notes = "This endpoint is used to delete a project by projectId.",
            response = ProjectDto.class,
            responseReference = "ResponseEntity<ProjectDto>",
            httpMethod = DELETE,
            produces = APPLICATION_JSON_VALUE)
    @DeleteMapping("/{projectId}")
    public ResponseEntity<HttpStatus> deleteProject(@PathVariable("projectId") long projectId) {
        projectService.deleteProject(projectId);
        return new ResponseEntity<>(NO_CONTENT);
    }

    @ApiOperation(value = "Edit a project",
            notes = "This endpoint is used to edit a project by projectId.",
            response = ProjectDto.class,
            responseReference = "ResponseEntity<ProjectDto>",
            httpMethod = PUT,
            produces = APPLICATION_JSON_VALUE)
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectDto> editProject(@PathVariable("projectId") long projectId, @RequestBody @Valid EditProjectCommand command) {
        Project project = projectService.editProject(projectId, command);
        return new ResponseEntity<>(modelMapper.map(project, ProjectDto.class), OK);
    }

    @ApiOperation(value = "Edit a project partially",
            notes = "This endpoint is used to edit a project partially by projectId.",
            response = ProjectDto.class,
            responseReference = "ResponseEntity<ProjectDto>",
            httpMethod = PATCH,
            produces = APPLICATION_JSON_VALUE)
    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectDto> editProjectPartially(@PathVariable("projectId") long projectId, @RequestBody @Valid EditProjectPartiallyCommand command) {
        Project project = projectService.editProjectPartially(projectId, command);
        return new ResponseEntity<>(modelMapper.map(project, ProjectDto.class), OK);
    }

    @ApiOperation(value = "Get members of a project",
            notes = "This endpoint is used to get members of a project by projectId.",
            response = MemberDto.class,
            responseReference = "ResponseEntity<Set<UserDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping("/{projectId}/members")
    public ResponseEntity<Set<MemberDto>> getProjectMembers(@PathVariable("projectId") long projectId) {
        return new ResponseEntity<>(projectService.findProjectMembers(projectId).stream().map(member -> modelMapper.map(member, MemberDto.class)).collect(toSet()), OK);
    }

    @ApiOperation(value = "Get tasks of a project",
            notes = "This endpoint is used to get tasks of a project by projectId.",
            response = TaskDto.class,
            responseReference = "ResponseEntity<Set<TaskDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping("/{projectId}/tasks")
    public ResponseEntity<Set<TaskDto>> getProjectTasks(@PathVariable("projectId") long projectId) {
        return new ResponseEntity<>(projectService.findProjectTasks(projectId).stream().map(task -> modelMapper.map(task, TaskDto.class)).collect(toSet()), OK);
    }

    @ApiOperation(value = "Get tasks of a project for a member",
            notes = "This endpoint is used to get tasks of a project for a member by member username.",
            response = TaskDto.class,
            responseReference = "ResponseEntity<Set<TaskDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping(path = "/{projectId}/tasks", params = "username")
    public ResponseEntity<Set<TaskDto>> getProjectTasksForMember(@PathVariable("projectId") long projectId, @NotNull(message = "USERNAME_NOT_NULL") @RequestParam("username") String username) {
        return new ResponseEntity<>(projectService.findProjectTasksForMember(projectId, username).stream()
                .map(task -> modelMapper.map(task, TaskDto.class))
                .collect(toSet()), OK);
    }

    @ApiOperation(value = "Discard member from a project",
            notes = "This endpoint is used to discard member from a project by project projectId and member username.",
            response = MemberDto.class,
            responseReference = "ResponseEntity<MemberDto>",
            httpMethod = DELETE,
            produces = APPLICATION_JSON_VALUE)
    @DeleteMapping(path = "/{projectId}/members", params = "username")
    public ResponseEntity<HttpStatus> discardMemberFromProject(@PathVariable("projectId") long projectId, @RequestParam("username") String username) {
        projectService.discardMemberFromProject(projectId, username);
        return new ResponseEntity<>(OK);
    }

    @ApiOperation(value = "Assign members to the project task",
            notes = "This endpoint is used to assign members to the project task by project projectId, task projectId and member usernames.",
            response = MemberDto.class,
            responseReference = "ResponseEntity<Set<MemberDto>>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PostMapping(path = "/{projectId}/task/{taskId}/members", params = "usernames")
    public ResponseEntity<Set<MemberDto>> assignMembersToTask(@PathVariable("projectId") long projectId, @PathVariable("taskId") long taskId, @RequestParam("usernames") Set<String> usernames) {
        Set<Member> members = projectService.assignMembersToTask(projectId, taskId, usernames);
        return new ResponseEntity<>(members.stream().map(member -> modelMapper.map(member, MemberDto.class)).collect(toSet()), OK);
    }


    @ApiOperation(value = "Add members to the project",
            notes = "This endpoint is used to add members to the project by project projectId and member usernames.",
            response = MemberDto.class,
            responseReference = "ResponseEntity<Set<MemberDto>>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PostMapping(path = "/{projectId}/members", params = "usernames")
    public ResponseEntity<Set<MemberDto>> addMembersToProject(@PathVariable("projectId") long projectId, @RequestParam("usernames") Set<String> usernames) {
        Set<Member> members = projectService.assignMembersToProject(projectId, usernames);
        return new ResponseEntity<>(members.stream().map(member -> modelMapper.map(member, MemberDto.class)).collect(toSet()), OK);
    }
}
