package com.kanwise.api_gateway.excpetion.handling;

import org.springframework.http.HttpStatus;

record ExceptionRule(Class<?> exceptionClass, HttpStatus status) {
}

