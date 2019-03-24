package com.example.scriptengine.service;

import com.example.scriptengine.exceptions.NotFoundException;
import com.example.scriptengine.model.TaskStage;
import com.example.scriptengine.model.dto.TaskResult;
import com.example.scriptengine.model.dto.TaskResultWidthLog;
import com.example.scriptengine.service.script.EngineLauncher;
import org.springframework.stereotype.Service;

import java.io.Writer;
import java.util.List;
import java.util.Map;
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

    public TaskService() {
        this.executorService = Executors.newFixedThreadPool(NUM_TREADS);
        this.tasks = new ConcurrentHashMap<>();
    }

    /**
     * Возвращает Runnable задания для последующего запуска
     *
     * @param script JavaScrip текст
     * @param engineLauncher EngineLauncher
     * @param scriptOutputWriter  Writer куда будет записываться stdout javascript
     * @return TaskExecutor
     */
    public TaskExecutor getTaskExecutor(String script, EngineLauncher engineLauncher, Writer scriptOutputWriter) {
        TaskExecutor taskExecutor = new TaskExecutor(script, engineLauncher, scriptOutputWriter);
        tasks.put(taskExecutor.getTaskId(), taskExecutor);
        return taskExecutor;
    }

    /**
     * Добавляет в пулл задание в котором исполняется Javascript.
     *
     * @param script JavaScrip текст
     * @param engineLauncher EngineLauncher
     * @return идентификатор задания
     */
    public String runUnblocked(String script, EngineLauncher engineLauncher) {
        TaskExecutor taskExecutor = new TaskExecutor(script, engineLauncher);
        tasks.put(taskExecutor.getTaskId(), taskExecutor);
        CompletableFuture<Void> future = CompletableFuture.runAsync(taskExecutor, executorService);
        taskExecutor.setFuture(future);

        return taskExecutor.getTaskId();
    }

    /**
     * Прерывает процесс
     *
     * @param taskId идентификатор задачи
     */
    public void interrupt(String taskId) {
        TaskExecutor task = getTaskById(taskId);
        Thread thread = task.getThread().get();
        if(task.getStage() == TaskStage.InProgress && thread != null) {
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

    private TaskExecutor getTaskById(String taskId)  {
        return tasks.computeIfAbsent(taskId, t -> {
            throw new NotFoundException("Task not found.");
        });
    }
}
