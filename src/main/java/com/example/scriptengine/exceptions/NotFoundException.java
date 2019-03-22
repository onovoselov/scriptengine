package com.example.scriptengine.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
