package com.example.scriptengine.service;

import com.example.scriptengine.config.AppProperties;
import com.example.scriptengine.exceptions.NotFoundException;
import com.example.scriptengine.exceptions.PermissionException;
import com.example.scriptengine.exceptions.ScriptCompileException;
import com.example.scriptengine.model.TaskLog;
import com.example.scriptengine.model.TaskStage;
import com.example.scriptengine.model.User;
import com.example.scriptengine.model.dto.TaskResult;
import com.example.scriptengine.model.dto.TaskResultWidthLog;
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
 * Реализует всю логику по запуску JavaScript используя javax.script.ScriptEngine и отслеживание процесса их исполнения
 */
@Service
public class TaskService {
    final private ExecutorService executorService;
    final private Map<String, TaskExecutor> tasks;
    final private ScriptEngine engine;
    final private AppProperties appProperties;

    public TaskService(ScriptEngine engine, AppProperties appProperties) {
        this.executorService = Executors.newFixedThreadPool(appProperties.getNumThreads());
        this.tasks = new ConcurrentHashMap<>();
        this.engine = engine;
        this.appProperties = appProperties;
    }

    /**
     * Run script in blocked mode
     *
     * @param scriptBody         Javascript body
     * @param scriptOutputWriter Writer куда будет записываться stdout javascript
     * @return TaskExecutor
     */
    public TaskExecutor runBlocked(String scriptBody, String scriptOwner, Writer scriptOutputWriter) throws ScriptCompileException {
        if (getActiveTaskCount() >= appProperties.getNumThreads())
            throw new NotFoundException("There are no free threads to execute the script. Try later.");

        TaskExecutor taskExecutor = new TaskExecutor(new ScriptEngineLauncher(scriptBody, scriptOwner, engine), appProperties, scriptOutputWriter);
        tasks.put(taskExecutor.getTaskId(), taskExecutor);
        CompletableFuture<Void> future = CompletableFuture.runAsync(taskExecutor, executorService);
        taskExecutor.setFuture(future);
        return taskExecutor;
    }

    /**
     * Adds to the executors pool and launches thread.
     *
     * @param scriptBody Javascript body
     * @return Task Id
     */
    public TaskExecutor runUnblocked(String scriptBody, String scriptOwner) throws ScriptCompileException {
        return runUnblocked(scriptBody, scriptOwner, null);
    }

    /**
     * Adds to the executors pool and launches thread and adds a state change observer.
     *
     * @param scriptBody Javascript body
     * @return Task Id
     */
    public TaskExecutor runUnblocked(String scriptBody, String scriptOwner, Observer changeStageObserver) throws ScriptCompileException {
        TaskExecutor taskExecutor = new TaskExecutor(new ScriptEngineLauncher(scriptBody, scriptOwner, engine), appProperties);
        if (changeStageObserver != null) {
            taskExecutor.addObserver(changeStageObserver);
        }

        tasks.put(taskExecutor.getTaskId(), taskExecutor);
        CompletableFuture<Void> future = CompletableFuture.runAsync(taskExecutor, executorService);
        taskExecutor.setFuture(future);

        return taskExecutor;
    }

    /**
     * Interrupts the thread
     *
     * @param taskId Task Id
     * @param user   User
     */
    public void interrupt(String taskId, User user) throws PermissionException, NotFoundException {
        TaskExecutor task = getTaskById(taskId, user);
        if (task.getStage() != TaskStage.Pending && task.getStage() != TaskStage.InProgress)
            throw new NotFoundException("Script is not active");

        Thread thread = task.getThread().get();
        if (task.getStage() == TaskStage.InProgress && thread != null) {
            task.interrupt();
        } else {
            task.cancel();
        }
    }


    /**
     * Return script body
     *
     * @param taskId Task Id
     * @param user   User
     * @return Script body
     */
    public String getTaskScriptBody(String taskId, User user) throws PermissionException {
        TaskExecutor task = getTaskById(taskId, user);
        return task.getEngineLauncher().getScriptBody();
    }


    /**
     * Return script output
     *
     * @param taskId Task Id
     * @param user   User
     * @return Script output
     */
    public String getTaskScriptOutput(String taskId, User user) throws PermissionException {
        TaskExecutor task = getTaskById(taskId, user);
        List<TaskLog> taskLogList = task.getTaskLogList().getAndDeleteItems();
        return taskLogList.stream()
                .map(TaskLog::toString)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Returns a list of tasks in a specific stage.
     *
     * @param stage task stage: Pending|InProgress|DoneOk|DoneError|Interrupted
     * @return List<TaskResult>
     */
    public List<TaskResult> getTasks(TaskStage stage) {
        return tasks.values().stream()
                .filter(task -> task.getStage() == stage)
                .map(TaskResult::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns all tasks
     *
     * @return List<TaskResult>
     */
    public List<TaskResult> getTasks() {
        return tasks.values().stream()
                .map(TaskResult::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns task information.
     *
     * @param taskId Task Id
     * @param user   User
     * @return TaskResultWidthLog
     */
    public TaskResultWidthLog getTaskResult(String taskId, User user) throws PermissionException {
        return new TaskResultWidthLog(getTaskById(taskId, user));
    }

    private long getActiveTaskCount() {
        return tasks.values().stream()
                .filter(task -> task.getStage() == TaskStage.InProgress)
                .count();
    }

    private TaskExecutor getTaskById(String taskId) {
        return tasks.computeIfAbsent(taskId, t -> {
            throw new NotFoundException("Task not found.");
        });
    }

    private TaskExecutor getTaskById(String taskId, User user) throws PermissionException {
        TaskExecutor task = getTaskById(taskId);

        if (!user.isAdmin() && !user.getUserName().equals(task.getEngineLauncher().getScriptOwner())) {
            throw new PermissionException("Permission denied");
        }

        return task;
    }
}
