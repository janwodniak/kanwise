package com.kanwise.kanwise_service.controller.task;

import com.kanwise.kanwise_service.error.handling.ExceptionHandling;
import com.kanwise.kanwise_service.model.task_statistics.TaskStatistics;
import com.kanwise.kanwise_service.model.task_statistics.dto.TaskStatisticsDto;
import com.kanwise.kanwise_service.service.statistics.task.ITaskStatisticsService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static javax.ws.rs.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RequestMapping("/task")
@RestController
public class TaskStatisticsController extends ExceptionHandling {

    private final ITaskStatisticsService taskStatisticsService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Get task statistics",
            notes = "Get task statistics by task id",
            response = TaskStatisticsDto.class,
            responseReference = "ResponseEntity<TaskStatisticsDto>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping("{taskId}/statistics")
    public ResponseEntity<TaskStatisticsDto> findTaskStatistics(@PathVariable("taskId") long taskId) {
        TaskStatistics taskStatistics = taskStatisticsService.getTaskStatistics(taskId);
        return new ResponseEntity<>(modelMapper.map(taskStatistics, TaskStatisticsDto.class), OK);
    }
}
