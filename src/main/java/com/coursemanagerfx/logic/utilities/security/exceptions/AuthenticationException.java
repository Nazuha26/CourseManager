package com.coursemanagerfx.logic.utilities.security.exceptions;

public class AuthenticationException extends SecurityException {
    public AuthenticationException(String message) {
        super(message);
    }
}