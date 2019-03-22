package com.example.scriptengine.model.dto;

import com.example.scriptengine.model.Task;
import com.example.scriptengine.model.TaskStage;

public class TaskResult {
    private String id;
    private TaskStage stage;
    private long startTime;
    private long stopTime;

    public TaskResult() {
    }

    public TaskResult(Task task) {
        this.id = task.getId();
        this.stage = task.getStage();
        this.startTime = task.getStartTime();
        this.stopTime = task.getStopTime();
    }

    public String getId() {
        return id;
    }

    public TaskStage getStage() {
        return stage;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }
}
