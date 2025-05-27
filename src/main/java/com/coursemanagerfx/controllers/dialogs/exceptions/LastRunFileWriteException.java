package com.coursemanagerfx.controllers.dialogs.exceptions;

public class LastRunFileWriteException extends RuntimeException {
    public LastRunFileWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}