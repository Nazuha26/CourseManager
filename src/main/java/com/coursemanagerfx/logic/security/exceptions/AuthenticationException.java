package com.coursemanagerfx.logic.security.exceptions;

public class AuthenticationException extends SecurityException {
    public AuthenticationException(String message) {
        super(message);
    }
}