package com.example.scriptengine.model.dto;

import com.example.scriptengine.controller.EngineController;
import com.example.scriptengine.model.TaskStage;
import com.example.scriptengine.service.TaskExecutor;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;
import java.time.LocalDateTime;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Relation(value = "task", collectionRelation = "tasks")
public class TaskResource extends ResourceSupport {
    private String taskId;
    private String owner;
    private TaskStage stage;
    private LocalDateTime startTime;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime stopTime;

    public TaskResource(TaskExecutor taskExecutor) {
        this.taskId = taskExecutor.getTaskId();
        this.owner = taskExecutor.getEngineLauncher().getScriptOwner();
        this.stage = taskExecutor.getStage();
        this.startTime = taskExecutor.getStartTime();
        this.stopTime = taskExecutor.getStopTime();
        add(linkTo(EngineController.class).withRel("scripts"));
        add(linkTo(EngineController.class).slash(taskId).slash("body").withRel("body"));
        add(linkTo(EngineController.class).slash(taskId).slash("output").withRel("output"));
        add(linkTo(EngineController.class).slash(taskId).withSelfRel());
    }

    public String getTaskId() {
        return taskId;
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

    public String getOwner() {
        return owner;
    }
}
