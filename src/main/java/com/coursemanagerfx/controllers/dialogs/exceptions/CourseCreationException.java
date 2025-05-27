package com.coursemanagerfx.controllers.dialogs.exceptions;

public class CourseCreationException extends RuntimeException {
    public CourseCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}