package com.example.scriptengine.service;

import com.example.scriptengine.controller.EngineController;
import com.example.scriptengine.exceptions.ThreadInterrupted;
import com.example.scriptengine.model.TaskLogArrayList;
import com.example.scriptengine.model.TaskLogList;
import com.example.scriptengine.model.TaskStage;
import com.example.scriptengine.service.script.EngineLauncher;
import com.example.scriptengine.service.script.writer.TaskLogWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TaskExecutor implements Runnable {
    static final Logger logger = LoggerFactory.getLogger(EngineController.class);

    private EngineLauncher engineLauncher;
    private Writer scriptOutputWriter;

    private String taskId;
    private String scriptContent;
    private TaskStage stage;
    private LocalDateTime  startTime;
    private LocalDateTime  stopTime;
    private TaskLogList taskLogList;
    private CompletableFuture<Void> future;
    private WeakReference<Thread> thread;

    public TaskExecutor(String scriptContent, EngineLauncher engineLauncher) {
        init(scriptContent, engineLauncher);
        this.engineLauncher = engineLauncher;
    }

    public TaskExecutor(String scriptContent, EngineLauncher engineLauncher, Writer scriptOutputWriter) {
        this(scriptContent, engineLauncher);
        this.scriptOutputWriter = scriptOutputWriter;
    }

    public void init(String scriptContent, EngineLauncher engineLauncher) {
        this.taskId = generateId();
        this.scriptContent = scriptContent;
        this.stage = TaskStage.Pending;
        this.engineLauncher = engineLauncher;
        this.taskLogList = new TaskLogArrayList();
        this.scriptOutputWriter = new TaskLogWriter(this.taskLogList);
    }

    @Override
    public void run() {
        final Thread currentThread = Thread.currentThread();
        currentThread.setName(taskId);
        thread = new WeakReference<>(currentThread);
        started();
        try {
            if(engineLauncher.launch(scriptContent, scriptOutputWriter)) {
                stopped();
            } else {
                error();
            }
        } catch (ThreadInterrupted e) {
            interrupted();
        } catch (IOException e) {
            logger.error("ScriptOutputWriter.write()", e);
        }

        try {
            scriptOutputWriter.close();
        } catch (IOException e) {
            logger.error("ScriptOutputWriter.close", e);
        }
    }

    private static String generateId() {
        String guid = UUID.randomUUID().toString();
        return  guid.substring(1, guid.length() - 2);
    }

    private synchronized void started() {
        stage = TaskStage.InProgress;
        startTime = LocalDateTime.now();
        logger.info("STARTED: " + taskId);
    }

    private synchronized void stopped() {
        stage = TaskStage.DoneOk;
        stopTime = LocalDateTime.now();
        logger.info("STOPPED: " + taskId);
    }

    private synchronized void error() {
        stage = TaskStage.DoneError;
        stopTime = LocalDateTime.now();
        logger.info("ERROR: " + taskId);
    }

    void interrupt() {
        try {
            scriptOutputWriter.write("The script was forcibly interrupted.");
            scriptOutputWriter.flush();
            interrupted();
        } catch (IOException e) {
            logger.error("ScriptOutputWriter.write()", e);
        }
    }

    void cancel() {
        if(stage == TaskStage.Pending) {
            future.cancel(true);
            interrupted();
        }
    }

    private synchronized void interrupted() {
        stage = TaskStage.Interrupted;
        stopTime = LocalDateTime.now();
        logger.info("INTERRUPTED: " + taskId);
    }

    public TaskStage getStage() {
        return stage;
    }

    public String getTaskId() {
        return taskId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getStopTime() {
        return stopTime;
    }

    public TaskLogList getTaskLogList() {
        return taskLogList;
    }

    CompletableFuture<Void> getFuture() {
        return future;
    }

    void setFuture(CompletableFuture<Void> future) {
        this.future = future;
    }

    WeakReference<Thread> getThread() {
        return thread;
    }
}
