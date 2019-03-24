package com.example.scriptengine.model;

import java.time.LocalDateTime;

/**
 * Для хранеия строки выводимой в stdout выполняющимся скриптом
 */
public class TaskLog {
    private LocalDateTime dateTime;
    private String message;

    /**
     * @param dateTime время сообщения
     * @param message тект сообщения
     */
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
