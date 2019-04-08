package com.example.scriptengine.model;

import java.time.LocalDateTime;

/** Для хранеия строки выводимой в stdout выполняющимся скриптом */
public class ScriptLog {
    private LocalDateTime dateTime;
    private String message;

    /**
     * @param dateTime время сообщения
     * @param message тект сообщения
     */
    public ScriptLog(LocalDateTime dateTime, String message) {
        this.dateTime = dateTime;
        this.message = message;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return dateTime + " " + message;
    }
}
