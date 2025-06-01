package com.coursemanagerfx.logic.utilities.exceptions;

public class NoInternetConnection extends RuntimeException {
    public NoInternetConnection(String message, Throwable cause) {
        super(message, cause);
    }
}