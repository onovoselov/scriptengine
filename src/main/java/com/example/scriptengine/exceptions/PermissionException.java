package com.example.scriptengine.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class PermissionException extends Exception {
    public PermissionException() {
    }

    public PermissionException(String message) {
        super(message);
    }
}
