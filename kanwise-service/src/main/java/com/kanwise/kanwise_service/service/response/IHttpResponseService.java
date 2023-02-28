package com.kanwise.kanwise_service.service.response;

import com.kanwise.kanwise_service.model.response.HttpResponse;
import org.springframework.http.HttpStatus;

public interface IHttpResponseService {
    HttpResponse generateHttpResponse(HttpStatus httpStatus, String message);

}
