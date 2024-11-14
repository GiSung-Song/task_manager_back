package com.taskmanager.myapp.exception;

public class CustomDeniedException extends RuntimeException {

    public CustomDeniedException(String message) {
        super(message);
    }

    public CustomDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
