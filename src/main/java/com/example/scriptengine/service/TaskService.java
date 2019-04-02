package com.example.scriptengine.service;

import com.example.scriptengine.exceptions.NotFoundException;
import com.example.scriptengine.exceptions.ScriptCompileException;
import com.example.scriptengine.model.TaskStage;
import com.example.scriptengine.model.dto.TaskResult;
import com.example.scriptengine.model.dto.TaskResultWidthLog;
import com.example.scriptengine.service.script.ScriptEngineLauncher;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
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
    final private static int NUM_TREADS = 10;
    final private ExecutorService executorService;
    final private Map<String, TaskExecutor> tasks;
    final private ScriptEngine engine;


    public TaskService() {
        this.executorService = Executors.newFixedThreadPool(NUM_TREADS);
        this.tasks = new ConcurrentHashMap<>();
        this.engine = new ScriptEngineManager().getEngineByName("Nashorn");
    }

    /**
     * Returns runnable jobs for later launch.
     *
     * @param scriptBody Javascript body
     * @param scriptOutputWriter Writer куда будет записываться stdout javascript
     * @return TaskExecutor
     */
    public TaskExecutor getTaskExecutor(String scriptBody, Writer scriptOutputWriter) throws ScriptCompileException {
        TaskExecutor taskExecutor = new TaskExecutor(new ScriptEngineLauncher(scriptBody, engine), scriptOutputWriter);
        tasks.put(taskExecutor.getTaskId(), taskExecutor);
        return taskExecutor;
    }

    /**
     * Adds to the executors pool and launches thread.
     *
     * @param scriptBody Javascript body
     * @return Task Id
     */
    public String runUnblocked(String scriptBody) throws ScriptCompileException {
        return runUnblocked(scriptBody, null);
    }

    /**
     * Adds to the executors pool and launches thread and adds a state change observer.
     *
     * @param scriptBody Javascript body
     * @return Task Id
     */
    public String runUnblocked(String scriptBody, Observer changeStageObserver) throws ScriptCompileException {
        TaskExecutor taskExecutor = new TaskExecutor(new ScriptEngineLauncher(scriptBody, engine));
        if(changeStageObserver != null) {
            taskExecutor.addObserver(changeStageObserver);
        }

        tasks.put(taskExecutor.getTaskId(), taskExecutor);
        CompletableFuture<Void> future = CompletableFuture.runAsync(taskExecutor, executorService);
        taskExecutor.setFuture(future);

        return taskExecutor.getTaskId();
    }

    /**
     * Interrupts the thread
     *
     * @param taskId Task Id
     */
    public void interrupt(String taskId) {
        TaskExecutor task = getTaskById(taskId);
        Thread thread = task.getThread().get();
        if (task.getStage() == TaskStage.InProgress && thread != null) {
            task.interrupt();
            thread.stop();
        } else {
            task.cancel();
        }
    }

    /**
     * Возвращает список задач находящихся в определенном состоянии
     *
     * @param stage сотояние задачи Pending|InProgress|DoneOk|DoneError|Interrupted
     * @return List<TaskResult>
     */
    public List<TaskResult> getTasks(TaskStage stage) {
        return tasks.values().stream()
                .filter(task -> task.getStage() == stage)
                .map(TaskResult::new)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает все задачи
     *
     * @return List<TaskResult>
     */
    public List<TaskResult> getTasks() {
        return tasks.values().stream()
                .map(TaskResult::new)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает информацию по конкретной задаче
     *
     * @param taskId идентификатор задачи
     * @return TaskResultWidthLog
     */
    public TaskResultWidthLog getTaskResult(String taskId) {
        return new TaskResultWidthLog(getTaskById(taskId));
    }

    public TaskExecutor getTaskById(String taskId) {
        return tasks.computeIfAbsent(taskId, t -> {
            throw new NotFoundException("Task not found.");
        });
    }
}
