package com.example.scriptengine.exceptions;

public class ThreadInterrupted extends RuntimeException {
    public ThreadInterrupted() {}

    public ThreadInterrupted(String message) {
        super(message);
    }
}
