package com.kanwise.user_service.controller.authentication;

import com.kanwise.user_service.model.authentication.request.LoginRequest;
import com.kanwise.user_service.model.authentication.response.LoginResponse;
import com.kanwise.user_service.model.user.dto.UserDto;
import com.kanwise.user_service.service.authentication.login.ILoginService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RequestMapping("/auth/login")
@RestController
public class LoginController {

    private final ILoginService loginService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Login",
            notes = "This endpoint is used to login. If login is successful, JWT token will be returned as a response header.",
            response = UserDto.class,
            responseReference = "ResponseEntity<UserDto>",
            httpMethod = "POST",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping
    public ResponseEntity<UserDto> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse loginResponse = loginService.login(request);
        return new ResponseEntity<>(modelMapper.map(loginResponse.user(), UserDto.class), loginResponse.jwtHeader(), OK);
    }
}
