package com.kanwise.user_service.controller.authentication;

import com.kanwise.user_service.model.authentication.request.RegisterRequest;
import com.kanwise.user_service.model.authentication.response.RegisterResponse;
import com.kanwise.user_service.model.authentication.response.dto.RegisterResponseDto;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.service.authentication.register.IRegisterService;
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

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RequestMapping("/auth/register")
@RestController
public class RegisterController {

    private final ModelMapper modelMapper;
    private final IRegisterService registerService;

    @ApiOperation(value = "Register",
            notes = "This endpoint is used to register. If registration is successful, user data will be returned. In case of enabled 2FA, OTP id will be returned as well.",
            response = RegisterResponseDto.class,
            responseReference = "ResponseEntity<RegisterResponseDto>",
            httpMethod = "POST",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping
    public ResponseEntity<RegisterResponseDto> register(@RequestBody @Valid RegisterRequest request) {
        RegisterResponse registerResponse = registerService.register(modelMapper.map(request, User.class));
        return new ResponseEntity<>(modelMapper.map(registerResponse, RegisterResponseDto.class), CREATED);
    }
}
