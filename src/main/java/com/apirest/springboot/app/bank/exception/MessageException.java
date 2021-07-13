package com.apirest.springboot.app.bank.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.apirest.springboot.app.bank.controllers.UserController;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MessageException  extends RuntimeException {
  private static final Logger logger = LoggerFactory.getLogger(UserController.class);
  
  public MessageException(String message) {
	  super(message);
	  logger.info(message);
  }
}
