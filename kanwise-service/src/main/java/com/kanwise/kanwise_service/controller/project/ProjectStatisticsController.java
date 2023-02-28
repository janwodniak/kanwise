package com.kanwise.kanwise_service.controller.project;

import com.kanwise.kanwise_service.error.handling.ExceptionHandling;
import com.kanwise.kanwise_service.model.project_statistics.ProjectStatistics;
import com.kanwise.kanwise_service.model.project_statistics.dto.ProjectStatisticsDto;
import com.kanwise.kanwise_service.model.task_statistics.dto.TaskStatisticsDto;
import com.kanwise.kanwise_service.service.statistics.project.IProjectStatisticsService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RequestMapping("/project")
@RestController
public class ProjectStatisticsController extends ExceptionHandling {

    private final IProjectStatisticsService projectStatisticsService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Get project statistics",
            notes = "This endpoint is used to get project statistics by project projectId.",
            response = ProjectStatisticsDto.class,
            responseReference = "ResponseEntity<ProjectStatisticsDto>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping("/{projectId}/statistics")
    public ResponseEntity<ProjectStatisticsDto> getProjectStatistics(@PathVariable("projectId") long projectId) {
        ProjectStatistics projectStatistics = projectStatisticsService.getProjectStatistics(projectId);
        return new ResponseEntity<>(modelMapper.map(projectStatistics, ProjectStatisticsDto.class), OK);
    }

    @ApiOperation(value = "Get project statistics for member",
            notes = "This endpoint is used to get project statistics by project projectId and member username.",
            response = ProjectStatisticsDto.class,
            responseReference = "ResponseEntity<ProjectStatisticsDto>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping(path = "/{projectId}/statistics", params = "username")
    public ResponseEntity<ProjectStatisticsDto> getProjectStatisticsForMember(@PathVariable("projectId") long projectId, @NotNull(message = "USERNAME_NOT_NULL") @RequestParam("username") String username) {
        ProjectStatistics projectStatistics = projectStatisticsService.getProjectStatisticsForMember(projectId, username);
        return new ResponseEntity<>(modelMapper.map(projectStatistics, ProjectStatisticsDto.class), OK);
    }

    @ApiOperation(value = "Get project tasks statistics of project",
            notes = "This endpoint is used to get project tasks statistics by project projectId.",
            response = TaskStatisticsDto.class,
            responseReference = "ResponseEntity<Set<TaskStatisticsDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping("/{projectId}/tasks/statistics")
    public ResponseEntity<Set<TaskStatisticsDto>> getProjectTaskStatistics(@PathVariable("projectId") long projectId) {
        return new ResponseEntity<>(projectStatisticsService.findProjectTasksStatistics(projectId).stream()
                .map(task -> modelMapper.map(task, TaskStatisticsDto.class)).collect(Collectors.toSet()), OK);
    }
}
