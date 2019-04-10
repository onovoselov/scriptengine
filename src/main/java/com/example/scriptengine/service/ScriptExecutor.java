package com.example.scriptengine.service;

import com.example.scriptengine.config.AppProperties;
import com.example.scriptengine.controller.EngineController;
import com.example.scriptengine.exceptions.ThreadInterrupted;
import com.example.scriptengine.model.ScriptLogArrayList;
import com.example.scriptengine.model.ScriptLogList;
import com.example.scriptengine.model.ScriptStage;
import com.example.scriptengine.service.script.EngineLauncher;
import com.example.scriptengine.service.script.writer.ScriptLogWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.util.Observable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** A task in which Javascript is executed and all information related to it is saved. */
public class ScriptExecutor extends Observable implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(EngineController.class);

    final private EngineLauncher engineLauncher;

    private Writer scriptOutputWriter;
    private String scriptId;
    private ScriptStage stage;
    private LocalDateTime startTime;
    private LocalDateTime stopTime;
    private ScriptLogList scriptLogList;
    private CompletableFuture<Void> future;
    private WeakReference<Thread> thread;
    private AppProperties appProperties;

    /**
     * Script executor
     *
     * @param engineLauncher Engine Launcher
     * @param appProperties App Properties
     */
    ScriptExecutor(EngineLauncher engineLauncher, AppProperties appProperties) {
        this.engineLauncher = engineLauncher;
        this.appProperties = appProperties;
        init();
    }

    /**
     * Script executor
     *
     * @param engineLauncher Engine Launcher
     * @param scriptOutputWriter Writer for stdout javascript
     */
    ScriptExecutor(
            EngineLauncher engineLauncher, AppProperties appProperties, Writer scriptOutputWriter) {
        this(engineLauncher, appProperties);
        this.scriptOutputWriter = scriptOutputWriter;
    }

    private void init() {
        this.scriptId = generateId();
        changeStage(ScriptStage.Pending);
        this.scriptLogList = new ScriptLogArrayList();
        this.scriptOutputWriter = new ScriptLogWriter(this.scriptLogList);
    }

    @Override
    public void run() {
        final Thread currentThread = Thread.currentThread();
        currentThread.setName(scriptId);
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
        changeStage(ScriptStage.InProgress);
        startTime = LocalDateTime.now();
        logger.info("STARTED: " + scriptId);
    }

    private synchronized void stopped() {
        changeStage(ScriptStage.DoneOk);
        stopTime = LocalDateTime.now();
        logger.info("STOPPED: " + scriptId);
    }

    private synchronized void error() {
        changeStage(ScriptStage.DoneError);
        stopTime = LocalDateTime.now();
        logger.info("ERROR: " + scriptId);
    }

    /**
     * First we try to close the standard output, if it does not help, then we interrupt it, if it
     * does not help, we stop the thread.
     */
    void interrupt() {
        try {
            scriptOutputWriter.write("The script was forcibly interrupted.");
            scriptOutputWriter.flush();
            // Close output
            scriptOutputWriter.close();
            awaitInterrupt();
            // Did not help
            if (stage == ScriptStage.InProgress) {
                Thread thread = getThread().get();
                if (thread != null) {
                    thread.interrupt();
                    awaitInterrupt();
                    // Did not help
                    thread.stop();
                    logger.info("THREAD STOP: " + scriptId);
                }
            }

            interrupted();
        } catch (IOException ex) {
            logger.error("ScriptOutputWriter.interrupt()", ex);
        }
    }

    private void awaitInterrupt() {
        try {
            for (int i = 0; i < 50 && stage == ScriptStage.InProgress; i++) {
                Thread.sleep(appProperties.getInterruptTimeout() / 50);
            }
        } catch (InterruptedException ex) {
            logger.error("awaitInterrupt", ex);
        }
    }

    void cancel() {
        if (stage == ScriptStage.Pending) {
            future.cancel(true);
            interrupted();
        }
    }

    private synchronized void interrupted() {
        if (stage != ScriptStage.Interrupted) {
            changeStage(ScriptStage.Interrupted);
            stopTime = LocalDateTime.now();
            logger.info("INTERRUPTED: " + scriptId);
        }
    }

    private void changeStage(ScriptStage stage) {
        this.stage = stage;
        setChanged();
        notifyObservers(stage);
    }

    public ScriptStage getStage() {
        return stage;
    }

    public String getScriptId() {
        return scriptId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getStopTime() {
        return stopTime;
    }

    public ScriptLogList getScriptLogList() {
        return scriptLogList;
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

    public EngineLauncher getEngineLauncher() {
        return engineLauncher;
    }
}
