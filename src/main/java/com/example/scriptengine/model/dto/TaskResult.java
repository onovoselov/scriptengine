package com.example.scriptengine.model.dto;

import com.example.scriptengine.model.TaskStage;
import com.example.scriptengine.service.TaskExecutor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Для вывода состояния работы скрипта
 */
public class TaskResult {
    private String id;
    private String owner;
    private TaskStage stage;
    private LocalDateTime startTime;
    private LocalDateTime stopTime;
    private List<TaskLink> links;

    TaskResult() {
        this.links = new ArrayList<>();
    }

    public TaskResult(TaskExecutor taskExecutor) {
        this();
        this.id = taskExecutor.getTaskId();
        this.owner = taskExecutor.getEngineLauncher().getScriptOwner();
        this.stage = taskExecutor.getStage();
        this.startTime = taskExecutor.getStartTime();
        this.stopTime = taskExecutor.getStopTime();
        links.add(new TaskLink("/task/" + this.id, "self"));
    }

    public String getId() {
        return id;
    }

    public TaskStage getStage() {
        return stage;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getStopTime() {
        return stopTime;
    }

    public List<TaskLink> getLinks() {
        return links;
    }

    public String getOwner() {
        return owner;
    }
}
