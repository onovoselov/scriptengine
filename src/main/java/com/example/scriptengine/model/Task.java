package com.example.scriptengine.model;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Task {
    private String id;
    private String content;
    private TaskStage stage;
    private long createTime;
    private long startTime;
    private long stopTime;
    private TaskLogList taskLogList;
    private CompletableFuture future;

    public Task() {
    }

    public Task(String id, String content) {
        this.id = id;
        this.content = content;
        this.stage = TaskStage.Pending;
        this.createTime = System.currentTimeMillis();
        this.taskLogList = new TaskLogArrayList();
    }

    public Task(String content) {
        this(generateId(), content);
    }

    private static String generateId() {
        String guid = UUID.randomUUID().toString();
        return  guid.substring(1, guid.length() - 2);
    }

    public synchronized void started() {
        stage = TaskStage.InProgress;
        startTime = System.currentTimeMillis();
        System.out.println("START: " + id);
    }

    public synchronized void stopped() {
        stage = TaskStage.DoneOk;
        stopTime = System.currentTimeMillis();
        System.out.println("STOP: " + id);
    }

    public synchronized void error() {
        stage = TaskStage.DoneError;
        stopTime = System.currentTimeMillis();
        System.out.println("STOP: " + id);
    }

    public synchronized void interrupted() {
        stage = TaskStage.Interrupted;
        stopTime = System.currentTimeMillis();
        System.out.println("INTERRUPT: " + id);
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public TaskStage getStage() {
        return stage;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public TaskLogList getTaskLogList() {
        return taskLogList;
    }

    public CompletableFuture getFuture() {
        return future;
    }

    public void setFuture(CompletableFuture future) {
        this.future = future;
    }
}
