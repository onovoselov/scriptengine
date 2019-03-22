package com.example.scriptengine.model;

import java.time.LocalDateTime;

public class TaskLog {
    private LocalDateTime dateTime;
    private String message;

    public TaskLog(LocalDateTime dateTime, String message) {
        this.dateTime = dateTime;
        this.message = message;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getMessage() {
        return message;
    }
}
