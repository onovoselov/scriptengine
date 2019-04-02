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
import java.util.Observable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Задача в которой происходит выполнение Javascript и хранеие всей связанной с этим информацией
 */
public class TaskExecutor extends Observable implements Runnable {
    static final private Logger logger = LoggerFactory.getLogger(EngineController.class);

    private EngineLauncher engineLauncher;
    private Writer scriptOutputWriter;
    private String taskId;
    private TaskStage stage;
    private LocalDateTime startTime;
    private LocalDateTime stopTime;
    private TaskLogList taskLogList;
    private CompletableFuture<Void> future;
    private WeakReference<Thread> thread;

    /**
     * @param engineLauncher EngineLauncher - исполнитель скрипта
     */
    TaskExecutor(EngineLauncher engineLauncher) {
        init(engineLauncher);
        this.engineLauncher = engineLauncher;
    }

    /**
     * @param engineLauncher     EngineLauncher - исполнитель скрипта
     * @param scriptOutputWriter Writer куда будет записываться stdout javascript
     */
    TaskExecutor(EngineLauncher engineLauncher, Writer scriptOutputWriter) {
        this(engineLauncher);
        this.scriptOutputWriter = scriptOutputWriter;
    }

    private void init(EngineLauncher engineLauncher) {
        this.taskId = generateId();
        changeStage(TaskStage.Pending);
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
            if (engineLauncher.launch(scriptOutputWriter)) {
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
        return guid.substring(1, guid.length() - 2);
    }

    private synchronized void started() {
        changeStage(TaskStage.InProgress);
        startTime = LocalDateTime.now();
        logger.info("STARTED: " + taskId);
    }

    private synchronized void stopped() {
        changeStage(TaskStage.DoneOk);
        stopTime = LocalDateTime.now();
        logger.info("STOPPED: " + taskId);
    }

    private synchronized void error() {
        changeStage(TaskStage.DoneError);
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
        if (stage == TaskStage.Pending) {
            future.cancel(true);
            interrupted();
        }
    }

    private synchronized void interrupted() {
        if(stage != TaskStage.Interrupted) {
            changeStage(TaskStage.Interrupted);
            stopTime = LocalDateTime.now();
            logger.info("INTERRUPTED: " + taskId);
        }
    }

    private void changeStage(TaskStage stage) {
        this.stage = stage;
        setChanged();
        notifyObservers(stage);
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

    void setFuture(CompletableFuture<Void> future) {
        this.future = future;
    }

    WeakReference<Thread> getThread() {
        return thread;
    }

    public CompletableFuture<Void> getFuture() {
        return future;
    }
}
