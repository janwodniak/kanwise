package com.kanwise.user_service.service.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kanwise.user_service.model.response.HttpResponse;
import org.springframework.http.HttpStatus;

public interface IHttpResponseService {

    HttpResponse generateHttpResponse(HttpStatus httpStatus, String message);

    String generateHttpResponseMessage(HttpStatus httpStatus, String message) throws JsonProcessingException;
}
