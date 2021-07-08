package com.apirest.springboot.app.bank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

//@ControllerAdvice
//@RestController
@RestControllerAdvice
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

@ExceptionHandler(MessageException.class)
public final ResponseEntity<ExceptionResponse> handleNotFoundException(MessageException ex, WebRequest request) {
 ExceptionResponse exceptionResponse = new ExceptionResponse(ex.getMessage());
 return new ResponseEntity<ExceptionResponse>(exceptionResponse, HttpStatus.NOT_ACCEPTABLE);
}

}