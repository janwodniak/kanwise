package com.kanwise.user_service.controller.user;

import com.kanwise.clients.user_service.user.model.UserDataDto;
import com.kanwise.user_service.error.handling.ExceptionHandling;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.service.user.IUserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserDataController extends ExceptionHandling {

    private final IUserService userService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Get user data",
            notes = "Get user data as key-value pairs",
            response = UserDataDto.class,
            responseReference = "ResponseEntity<UserDataDto>",
            httpMethod = "GET",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @GetMapping("/{username}/data")
    public ResponseEntity<UserDataDto> findUserDataByUsername(@PathVariable("username") String username) {
        User user = userService.findByUsername(username);
        return new ResponseEntity<>(modelMapper.map(user, UserDataDto.class), OK);
    }
}
