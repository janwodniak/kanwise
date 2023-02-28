package com.kanwise.clients.user_service.user.client;

import com.kanwise.clients.user_service.user.model.UserDataDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "user-service", path = "/user")
public interface UserClient {

    @GetMapping("/{username}/data")
    ResponseEntity<UserDataDto> getUserData(@PathVariable("username") String username);
}
