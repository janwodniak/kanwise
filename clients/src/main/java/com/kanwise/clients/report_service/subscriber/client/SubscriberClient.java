package com.kanwise.clients.report_service.subscriber.client;

import com.kanwise.clients.report_service.subscriber.model.CreateSubscriberCommand;
import com.kanwise.clients.report_service.subscriber.model.EditSubscriberPartiallyCommand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(value = "report-service", path = "/subscriber")
public interface SubscriberClient {

    @PostMapping
    ResponseEntity<SubscriberDto> addSubscriber(CreateSubscriberCommand command);

    @PutMapping("/{username}")
    ResponseEntity<SubscriberDto> editSubscriberPartially(@PathVariable("username") String username, EditSubscriberPartiallyCommand command);

    @DeleteMapping("/{username}")
    ResponseEntity<HttpStatus> deleteSubscriber(@PathVariable("username") String username);
}
