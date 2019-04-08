package com.example.scriptengine.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ScriptRuntimeException extends Exception {
    public ScriptRuntimeException() {}

    public ScriptRuntimeException(String message) {
        super(message);
    }
}
