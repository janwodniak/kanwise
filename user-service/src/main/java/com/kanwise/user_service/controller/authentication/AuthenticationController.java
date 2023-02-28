package com.kanwise.user_service.controller.authentication;

import com.kanwise.user_service.model.jwt.TokenValidationRequest;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.model.user.dto.UserDto;
import com.kanwise.user_service.service.authentication.IAuthenticationService;
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
@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final ModelMapper modelMapper;
    private final IAuthenticationService authenticationService;

    @ApiOperation(value = "Validate JWT token",
            notes = "This endpoint is used to validate JWT token. If token is valid, user data will be returned.",
            response = UserDto.class,
            responseReference = "ResponseEntity<UserDto>",
            httpMethod = "POST",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping("/token/validate")
    public ResponseEntity<UserDto> validateToken(@RequestBody @Valid TokenValidationRequest tokenValidationRequest) {
        User user = authenticationService.validateJwtToken(tokenValidationRequest.token());
        return new ResponseEntity<>(modelMapper.map(user, UserDto.class), OK);
    }
}
