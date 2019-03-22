package com.example.scriptengine.model.dto;

public class TaskStart {
    private String script;
    private boolean blocked;

    public TaskStart() {
    }

    public TaskStart(String script, boolean blocked) {
        this.script = script;
        this.blocked = blocked;
    }

    public String getScript() {
        return script;
    }

    public boolean isBlocked() {
        return blocked;
    }
}
