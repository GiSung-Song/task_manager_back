package com.taskmanager.myapp.exception;

public class CustomInternalException extends RuntimeException {
    public CustomInternalException(String message) {
        super(message);
    }

    public CustomInternalException(String message, Throwable cause) {
        super(message, cause);
    }
}