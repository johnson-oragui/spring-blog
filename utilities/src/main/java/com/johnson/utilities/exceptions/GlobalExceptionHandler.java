package com.johnson.utilities.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<?> handleIllegalArg(BadRequestException exc) {
    return errorResponse(HttpStatus.BAD_REQUEST, exc.getMessage());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException exc) {
    return errorResponse(HttpStatus.NOT_FOUND, exc.getMessage());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<?> handleNotSupportedRequestMethod(HttpRequestMethodNotSupportedException exc) {
    return errorResponse(HttpStatus.METHOD_NOT_ALLOWED, exc.getMessage());
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<?> handleIllegalContentType(HttpMediaTypeNotSupportedException exc) {
    return errorResponse(HttpStatus.BAD_REQUEST, exc.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<?> handleNotFound(NotFoundException exc) {
    return errorResponse(HttpStatus.NOT_FOUND, exc.getMessage());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<?> handleConflictException(ConflictException exc) {
    return errorResponse(HttpStatus.CONFLICT, exc.getMessage());
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<?> handleValidationException(ValidationException exc) {
    return errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, exc.getMessage());
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<?> handleUnauthorizedException(UnauthorizedException exc) {
    return errorResponse(HttpStatus.UNAUTHORIZED, exc.getMessage());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<?> handleForbiddenException(ForbiddenException exc) {
    return errorResponse(HttpStatus.FORBIDDEN, exc.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException exc) {
    String errorMessage = "Malformed JSON request";
    if (exc.getMessage() != null && exc.getMessage().contains("JSON parse error")) {
      errorMessage = "JSON parse error: " + exc.getMessage()
          .substring(exc.getMessage().indexOf("JSON parse error:") + "JSON parse error:".length()).trim();
    }

    return errorResponse(HttpStatus.BAD_REQUEST, errorMessage);
  }

  // for handling jakarta.validation (@Valid failures)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    return new ResponseEntity<>(Map.of(
        "status", 422,
        "error", "Validation error",
        "message", "One or more fields are invalid",
        "data", errors), HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneral(Exception exc) {
    exc.printStackTrace();
    return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
  }

  private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message);
    return new ResponseEntity<>(body, status);
  }
}
