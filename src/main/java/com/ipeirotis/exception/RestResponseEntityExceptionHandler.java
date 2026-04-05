package com.ipeirotis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.text.ParseException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

	@ExceptionHandler(value = { ResourceNotFoundException.class })
	protected ResponseEntity<Object> resourceNotFound(ResourceNotFoundException e, WebRequest request) {
		return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(value = { ValidationException.class })
	protected ResponseEntity<Object> notValid(ValidationException e, WebRequest request) {
		return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = { ParseException.class })
	protected ResponseEntity<Object> parseError(ParseException e, WebRequest request) {
		return buildResponse("Invalid date format: " + e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = { IllegalArgumentException.class })
	protected ResponseEntity<Object> illegalArgument(IllegalArgumentException e, WebRequest request) {
		return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = { MturkException.class })
	protected ResponseEntity<Object> mturkError(MturkException e, WebRequest request) {
		log.error("MTurk API error", e);
		return buildResponse("MTurk service error: " + e.getMessage(), HttpStatus.BAD_GATEWAY);
	}

	@ExceptionHandler(value = { TaskEnqueueException.class })
	protected ResponseEntity<Object> taskEnqueueError(TaskEnqueueException e, WebRequest request) {
		log.error("Cloud Tasks enqueue error", e);
		return buildResponse("Failed to enqueue task: " + e.getMessage(), HttpStatus.BAD_GATEWAY);
	}

	@ExceptionHandler(value = { Exception.class })
	protected ResponseEntity<Object> handleAll(Exception e, WebRequest request) {
		log.error("Unhandled exception: " + request.getDescription(false), e);
		return buildResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<Object> buildResponse(String message, HttpStatus status) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTimestamp(new Date());
		errorResponse.setMessage(message);
		errorResponse.setStatus(status.value());
		return new ResponseEntity<>(errorResponse, status);
	}
}
