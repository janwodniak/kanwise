package com.kanwise.clients.kanwise_service.member;

import com.kanwise.clients.kanwise_service.member.model.CreateMemberRequest;
import com.kanwise.clients.kanwise_service.member.model.EditMemberPartiallyCommand;
import com.kanwise.clients.kanwise_service.member.model.MemberDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "kanwise-service", path = "/member")
public interface MemberClient {

    @PostMapping
    ResponseEntity<MemberDto> addMember(@RequestBody CreateMemberRequest member);

    @PutMapping("/{username}")
    ResponseEntity<MemberDto> editMemberPartially(@PathVariable("username") String username, EditMemberPartiallyCommand command);

    @DeleteMapping("/{username}")
    ResponseEntity<HttpStatus> deleteMember(@PathVariable("username") String username);
}
