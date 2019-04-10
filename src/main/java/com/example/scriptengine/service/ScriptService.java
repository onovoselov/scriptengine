package com.example.scriptengine.service;

import com.example.scriptengine.config.AppProperties;
import com.example.scriptengine.exceptions.NotAcceptableException;
import com.example.scriptengine.exceptions.NotFoundException;
import com.example.scriptengine.exceptions.PermissionException;
import com.example.scriptengine.exceptions.ScriptRuntimeException;
import com.example.scriptengine.model.ScriptLog;
import com.example.scriptengine.model.ScriptStage;
import com.example.scriptengine.model.User;
import com.example.scriptengine.model.dto.ScriptResourceResult;
import com.example.scriptengine.model.dto.ScriptResourceResultWidthLog;
import com.example.scriptengine.service.script.ScriptEngineLauncher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Реализует всю логику по запуску JavaScript используя javax.script.ScriptEngine и отслеживание
 * процесса их исполнения
 */
@Service
public class ScriptService {
    private final ExecutorService executorService;
    private final Map<String, ScriptExecutor> scripts;
    private final ScriptEngine engine;
    private final AppProperties appProperties;
    private final AtomicInteger numberActiveThreads;
    private final Observer changeStageObserver;

    public ScriptService(
            ScriptEngine engine, AppProperties appProperties, MeterRegistry meterRegistry) {
        this.executorService = Executors.newFixedThreadPool(appProperties.getNumThreads());
        this.scripts = new ConcurrentHashMap<>();
        this.engine = engine;
        this.appProperties = appProperties;
        numberActiveThreads = meterRegistry.gauge("numberActiveThreads", new AtomicInteger(0));
        changeStageObserver = (o, arg) -> {
            assert numberActiveThreads != null;
            if (arg instanceof ScriptStage) {
                ScriptStage stage = (ScriptStage) arg;
                if (stage == ScriptStage.InProgress) {
                    numberActiveThreads.getAndIncrement();
                } else if (stage == ScriptStage.Interrupted || stage == ScriptStage.DoneOk  || stage == ScriptStage.DoneError) {
                    numberActiveThreads.getAndDecrement();
                }
            }
        };
    }

    /**
     * Run script in blocked mode
     *
     * @param scriptBody Javascript body
     * @param scriptOutputWriter Writer куда будет записываться stdout javascript
     * @return ScriptExecutor
     */
    public ScriptExecutor runBlocked(
            String scriptBody, String scriptOwner, Writer scriptOutputWriter)
            throws ScriptRuntimeException {
        if (getActiveScriptCount() >= appProperties.getNumThreads())
            throw new NotFoundException(
                    "There are no free threads to execute the script. Try later.");

        ScriptExecutor scriptExecutor =
                new ScriptExecutor(
                        new ScriptEngineLauncher(scriptBody, scriptOwner, engine),
                        appProperties,
                        scriptOutputWriter);
        scripts.put(scriptExecutor.getScriptId(), scriptExecutor);
        CompletableFuture<Void> future =
                CompletableFuture.runAsync(scriptExecutor, executorService);
        scriptExecutor.setFuture(future);
        if (changeStageObserver != null) {
            scriptExecutor.addObserver(changeStageObserver);
        }

        return scriptExecutor;
    }


    /**
     * Adds to the executors pool and launches thread.
     *
     * @param scriptBody Javascript body
     * @return Script Id
     */
    public ScriptExecutor runUnblocked(String scriptBody, String scriptOwner)
            throws ScriptRuntimeException {
        return runUnblocked(scriptBody, scriptOwner, changeStageObserver);
    }

    /**
     * Adds to the executors pool and launches thread and adds a state change observer.
     *
     * @param scriptBody Javascript body
     * @return Script Id
     */
    public ScriptExecutor runUnblocked(
            String scriptBody, String scriptOwner, Observer changeStageObserver)
            throws ScriptRuntimeException {
        ScriptExecutor scriptExecutor =
                new ScriptExecutor(
                        new ScriptEngineLauncher(scriptBody, scriptOwner, engine),
                        appProperties);
        if (changeStageObserver != null) {
            scriptExecutor.addObserver(changeStageObserver);
        }

        scripts.put(scriptExecutor.getScriptId(), scriptExecutor);
        CompletableFuture<Void> future =
                CompletableFuture.runAsync(scriptExecutor, executorService);
        scriptExecutor.setFuture(future);

        return scriptExecutor;
    }

    /**
     * Interrupts the thread
     *
     * @param scriptId Script Id
     * @param user User
     */
    public void interrupt(String scriptId, User user)
            throws PermissionException, NotFoundException, NotAcceptableException {
        ScriptExecutor scriptExecutor = getScriptExecutorById(scriptId, user);
        if (scriptExecutor.getStage() != ScriptStage.Pending
                && scriptExecutor.getStage() != ScriptStage.InProgress)
            throw new NotAcceptableException("Script is not active");

        Thread thread = scriptExecutor.getThread().get();
        if (scriptExecutor.getStage() == ScriptStage.InProgress && thread != null) {
            scriptExecutor.interrupt();
        } else {
            scriptExecutor.cancel();
        }
    }

    /**
     * Return script body
     *
     * @param scriptId Script Id
     * @param user User
     * @return Script body
     */
    public String getScriptBody(String scriptId, User user) throws PermissionException {
        ScriptExecutor scriptExecutor = getScriptExecutorById(scriptId, user);
        return scriptExecutor.getEngineLauncher().getScriptBody();
    }

    /**
     * Return script output
     *
     * @param scriptId Script Id
     * @param user User
     * @return Script output
     */
    public String getScriptOutput(String scriptId, User user) throws PermissionException {
        ScriptExecutor scriptExecutor = getScriptExecutorById(scriptId, user);
        List<ScriptLog> scriptLogList = scriptExecutor.getScriptLogList().getAndDeleteItems();
        return scriptLogList.stream().map(ScriptLog::toString).collect(Collectors.joining("\n"));
    }

    /**
     * Returns a list of scripts in a specific stage.
     *
     * @param stage script stage: Pending|InProgress|DoneOk|DoneError|Interrupted
     * @return List<ScriptResourceResult>
     */
    public List<ScriptResourceResult> getScripts(ScriptStage stage) {
        return scripts.values().stream()
                .filter(scriptExecutor -> scriptExecutor.getStage() == stage)
                .map(ScriptResourceResult::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns all scripts
     *
     * @return List<ScriptResourceResult>
     */
    public List<ScriptResourceResult> getScripts() {
        return scripts.values().stream()
                .map(ScriptResourceResult::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns script information.
     *
     * @param scriptId Script Id
     * @param user User
     * @return ScriptResourceResultWidthLog
     */
    public ScriptResourceResultWidthLog getScriptResult(String scriptId, User user)
            throws PermissionException {
        return new ScriptResourceResultWidthLog(getScriptExecutorById(scriptId, user));
    }

    private long getActiveScriptCount() {
        return scripts.values().stream()
                .filter(scriptExecutor -> scriptExecutor.getStage() == ScriptStage.InProgress)
                .count();
    }

    private ScriptExecutor getScriptExecutorById(String scriptId) {
        return scripts.computeIfAbsent(
                scriptId,
                t -> {
                    throw new NotFoundException("Script not found.");
                });
    }

    private ScriptExecutor getScriptExecutorById(String scriptId, User user)
            throws PermissionException {
        ScriptExecutor scriptExecutor = getScriptExecutorById(scriptId);

        if (!user.isAdmin()
                && !user.getUserName()
                        .equals(scriptExecutor.getEngineLauncher().getScriptOwner())) {
            throw new PermissionException("Permission denied");
        }

        return scriptExecutor;
    }
}
