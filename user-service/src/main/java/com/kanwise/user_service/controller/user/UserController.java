package com.kanwise.user_service.controller.user;

import com.kanwise.user_service.error.handling.ExceptionHandling;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.model.user.command.CreateUserCommand;
import com.kanwise.user_service.model.user.command.CreateUserPageCommand;
import com.kanwise.user_service.model.user.command.EditUserCommand;
import com.kanwise.user_service.model.user.command.EditUserPartiallyCommand;
import com.kanwise.user_service.model.user.dto.UserDto;
import com.kanwise.user_service.service.user.IUserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
public class UserController extends ExceptionHandling {

    private final IUserService userService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Get single users",
            notes = "Get single users",
            response = UserDto.class,
            responseContainer = "List",
            responseReference = "ResponseEntity<List<UserDto>>",
            httpMethod = "GET",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findUserById(@PathVariable("id") long id) {
        User user = userService.findUserById(id);
        return new ResponseEntity<>(modelMapper.map(user, UserDto.class), OK);
    }

    @ApiOperation(value = "Get multiple users",
            notes = "Get multiple users",
            response = UserDto.class,
            responseContainer = "Page",
            responseReference = "ResponseEntity<Page<UserDto>>",
            httpMethod = "GET",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping
    public ResponseEntity<Page<UserDto>> findUsers(@RequestParam Optional<String> lastName, @Valid CreateUserPageCommand command) {
        Page<User> users = userService.findUsers(lastName.orElse(""), modelMapper.map(command, Pageable.class));
        return new ResponseEntity<>(users.map(user -> modelMapper.map(user, UserDto.class)), OK);
    }

    @ApiOperation(value = "Get users by usernames",
            notes = "Get users by usernames",
            response = UserDto.class,
            responseContainer = "List",
            responseReference = "ResponseEntity<List<UserDto>>",
            httpMethod = "GET",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping(params = "usernames")
    public ResponseEntity<List<UserDto>> findUsersByUsernames(@RequestParam("usernames") List<String> usernames) {
        Set<User> users = userService.findUsersByUsernames(usernames);
        return new ResponseEntity<>(users.stream().map(user -> modelMapper.map(user, UserDto.class)).toList(), OK);
    }

    @ApiOperation(value = "Create user",
            notes = "Create user",
            response = UserDto.class,
            responseReference = "ResponseEntity<UserDto>",
            httpMethod = "POST",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER_WRITE')")
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid CreateUserCommand command) {
        User user = userService.createUser(modelMapper.map(command, User.class), true, true);
        return new ResponseEntity<>(modelMapper.map(user, UserDto.class), CREATED);
    }

    @ApiOperation(value = "Delete user",
            notes = "Delete user",
            response = HttpStatus.class,
            responseReference = "ResponseEntity<HttpStatus>",
            httpMethod = "DELETE",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@authenticationFacade.isUserByIdAndHasAuthority(#id, 'USER_DELETE') or @authenticationFacade.admin")
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable("id") long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(NO_CONTENT);
    }

    @ApiOperation(value = "Update user",
            notes = "Update user",
            response = UserDto.class,
            responseReference = "ResponseEntity<UserDto>",
            httpMethod = "PUT",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@authenticationFacade.isUserByIdAndHasAuthority(#id, 'USER_WRITE') or @authenticationFacade.admin")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> editUser(@PathVariable("id") long id, @RequestBody @Valid EditUserCommand command) {
        User user = userService.editUser(id, command);
        return new ResponseEntity<>(modelMapper.map(user, UserDto.class), OK);
    }

    @ApiOperation(value = "Update user",
            notes = "Update user partially",
            response = UserDto.class,
            responseReference = "ResponseEntity<UserDto>",
            httpMethod = "PATCH",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@authenticationFacade.isUserByIdAndHasAuthority(#id, 'USER_WRITE') or @authenticationFacade.admin")
    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> editUserPartially(@PathVariable("id") long id, @RequestBody @Valid EditUserPartiallyCommand command) {
        User user = userService.editUserPartially(id, command);
        return new ResponseEntity<>(modelMapper.map(user, UserDto.class), OK);
    }
}
