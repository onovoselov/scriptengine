package com.example.scriptengine.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ScriptCompileException extends Exception {
    public ScriptCompileException() {
    }

    public ScriptCompileException(String message) {
        super(message);
    }
}
