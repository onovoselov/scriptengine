package com.example.scriptengine.model.dto;

import com.example.scriptengine.controller.EngineController;
import com.example.scriptengine.model.ScriptStage;
import com.example.scriptengine.service.ScriptExecutor;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;
import java.time.LocalDateTime;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Relation(value = "script", collectionRelation = "sccripts")
public class ScriptResourceResult extends ResourceSupport {
    private String scriptId;
    private String owner;
    private ScriptStage stage;
    private LocalDateTime startTime;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime stopTime;

    public ScriptResourceResult(ScriptExecutor scriptExecutor) {
        this.scriptId = scriptExecutor.getScriptId();
        this.owner = scriptExecutor.getEngineLauncher().getScriptOwner();
        this.stage = scriptExecutor.getStage();
        this.startTime = scriptExecutor.getStartTime();
        this.stopTime = scriptExecutor.getStopTime();
        add(linkTo(EngineController.class).withRel("scripts"));
        add(linkTo(EngineController.class).slash(scriptId).slash("body").withRel("body"));
        add(linkTo(EngineController.class).slash(scriptId).slash("output").withRel("output"));
        add(linkTo(EngineController.class).slash(scriptId).withSelfRel());
    }

    public String getScriptId() {
        return scriptId;
    }

    public ScriptStage getStage() {
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
