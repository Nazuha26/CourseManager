package com.coursemanagerfx.logic.security.exceptions;

import java.io.IOException;

public class DecryptionException extends IOException {
    public DecryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}