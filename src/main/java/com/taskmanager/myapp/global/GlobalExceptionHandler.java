package com.taskmanager.myapp.global;

import com.taskmanager.myapp.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // resource not found exception
    @ExceptionHandler(ResourceNotfoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotfoundException exception) {
        return createErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    // data conflict exception
    @ExceptionHandler(DataConflictException.class)
    public ResponseEntity<Map<String, String>> handleDataConflictException(DataConflictException exception) {
        return createErrorResponse(exception.getMessage(), HttpStatus.CONFLICT);
    }

    // auth exception
    @ExceptionHandler(CustomAuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(CustomAuthException exception) {
        return createErrorResponse(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // 403 exception
    @ExceptionHandler(CustomDeniedException.class)
    public ResponseEntity<Map<String, String>> handleDeniedException(CustomDeniedException exception) {
        return createErrorResponse(exception.getMessage(), HttpStatus.FORBIDDEN);
    }

    // BadRequest exception
    @ExceptionHandler(CustomBadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequestException(CustomBadRequestException exception) {
        return createErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Internal exception
    @ExceptionHandler(CustomInternalException.class)
    public ResponseEntity<Map<String, String>> handleInternalException(CustomInternalException exception) {
        return createErrorResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // validation exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Map<String, String>> createErrorResponse(String message, HttpStatus status) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return new ResponseEntity<>(errorResponse, status);
    }
}
