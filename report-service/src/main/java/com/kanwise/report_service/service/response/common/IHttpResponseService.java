package com.kanwise.report_service.service.response.common;

import com.kanwise.report_service.model.response.HttpResponse;
import org.springframework.http.HttpStatus;

public interface IHttpResponseService {
    HttpResponse generateHttpResponse(HttpStatus httpStatus, String message);
}
