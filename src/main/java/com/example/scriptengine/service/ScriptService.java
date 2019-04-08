package com.example.scriptengine.service;

import com.example.scriptengine.config.AppProperties;
import com.example.scriptengine.exceptions.NotFoundException;
import com.example.scriptengine.exceptions.PermissionException;
import com.example.scriptengine.exceptions.ScriptCompileException;
import com.example.scriptengine.model.ScriptLog;
import com.example.scriptengine.model.ScriptStage;
import com.example.scriptengine.model.User;
import com.example.scriptengine.model.dto.ScriptResource;
import com.example.scriptengine.model.dto.ScriptResourceWidthLog;
import com.example.scriptengine.service.script.ScriptEngineLauncher;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.concurrent.*;
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

    public ScriptService(ScriptEngine engine, AppProperties appProperties) {
        this.executorService = Executors.newFixedThreadPool(appProperties.getNumThreads());
        this.scripts = new ConcurrentHashMap<>();
        this.engine = engine;
        this.appProperties = appProperties;
    }

    /**
     * Run script in blocked mode
     *
     * @param scriptBody Javascript body
     * @param scriptOutputWriter Writer куда будет записываться stdout javascript
     * @return ScriptExecutor
     */
    public ScriptExecutor runBlocked(String scriptBody, String scriptOwner, Writer scriptOutputWriter)
            throws ScriptCompileException {
        if (getActiveScriptCount() >= appProperties.getNumThreads())
            throw new NotFoundException(
                    "There are no free threads to execute the script. Try later.");

        ScriptExecutor scriptExecutor =
                new ScriptExecutor(
                        new ScriptEngineLauncher(scriptBody, scriptOwner, engine),
                        appProperties,
                        scriptOutputWriter);
        scripts.put(scriptExecutor.getScriptId(), scriptExecutor);
        CompletableFuture<Void> future = CompletableFuture.runAsync(scriptExecutor, executorService);
        scriptExecutor.setFuture(future);
        return scriptExecutor;
    }

    /**
     * Adds to the executors pool and launches thread.
     *
     * @param scriptBody Javascript body
     * @return Script Id
     */
    public ScriptExecutor runUnblocked(String scriptBody, String scriptOwner)
            throws ScriptCompileException {
        return runUnblocked(scriptBody, scriptOwner, null);
    }

    /**
     * Adds to the executors pool and launches thread and adds a state change observer.
     *
     * @param scriptBody Javascript body
     * @return Script Id
     */
    public ScriptExecutor runUnblocked(
            String scriptBody, String scriptOwner, Observer changeStageObserver)
            throws ScriptCompileException {
        ScriptExecutor scriptExecutor =
                new ScriptExecutor(
                        new ScriptEngineLauncher(scriptBody, scriptOwner, engine), appProperties);
        if (changeStageObserver != null) {
            scriptExecutor.addObserver(changeStageObserver);
        }

        scripts.put(scriptExecutor.getScriptId(), scriptExecutor);
        CompletableFuture<Void> future = CompletableFuture.runAsync(scriptExecutor, executorService);
        scriptExecutor.setFuture(future);

        return scriptExecutor;
    }

    /**
     * Interrupts the thread
     *
     * @param scriptId Script Id
     * @param user User
     */
    public void interrupt(String scriptId, User user) throws PermissionException, NotFoundException {
        ScriptExecutor scriptExecutor = getScriptExecutorById(scriptId, user);
        if (scriptExecutor.getStage() != ScriptStage.Pending && scriptExecutor.getStage() != ScriptStage.InProgress)
            throw new NotFoundException("Script is not active");

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
     * @return List<ScriptResource>
     */
    public List<ScriptResource> getScripts(ScriptStage stage) {
        return scripts.values().stream()
                .filter(scriptExecutor -> scriptExecutor.getStage() == stage)
                .map(ScriptResource::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns all scripts
     *
     * @return List<ScriptResource>
     */
    public List<ScriptResource> getScripts() {
        return scripts.values().stream().map(ScriptResource::new).collect(Collectors.toList());
    }

    /**
     * Returns script information.
     *
     * @param scriptId Script Id
     * @param user User
     * @return ScriptResourceWidthLog
     */
    public ScriptResourceWidthLog getScriptResult(String scriptId, User user) throws PermissionException {
        return new ScriptResourceWidthLog(getScriptExecutorById(scriptId, user));
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

    private ScriptExecutor getScriptExecutorById(String scriptId, User user) throws PermissionException {
        ScriptExecutor scriptExecutor = getScriptExecutorById(scriptId);

        if (!user.isAdmin()
                && !user.getUserName().equals(scriptExecutor.getEngineLauncher().getScriptOwner())) {
            throw new PermissionException("Permission denied");
        }

        return scriptExecutor;
    }
}
