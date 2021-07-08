package com.apirest.springboot.app.bank.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apirest.springboot.app.bank.controllers.UserController;

public class ExceptionResponse {
	  private String mensaje;
		private static Logger logger = LoggerFactory.getLogger(UserController.class);


	  public ExceptionResponse(String message) {
	    super();
	    logger.debug(message);
	    this.mensaje = message;
	  }

	  public String getMensaje() {
	    return mensaje;
	  }

	}
