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

@Service
public class TaskService {
    final private static int NUM_TREADS = 10;
    final private ExecutorService executorService;
    final private Map<String, TaskExecutor> tasks;

    public TaskService() {
        this.executorService = Executors.newFixedThreadPool(NUM_TREADS);
        this.tasks = new ConcurrentHashMap<>();
    }

    public Runnable createTaskExecutor(String script, EngineLauncher engineLauncher, Writer writer) {
        TaskExecutor taskExecutor = new TaskExecutor(script, engineLauncher, writer);
        tasks.put(taskExecutor.getTaskId(), taskExecutor);
        return taskExecutor;
    }

    public String runUnblocked(String script, EngineLauncher engineLauncher) {
        TaskExecutor taskExecutor = new TaskExecutor(script, engineLauncher);
        tasks.put(taskExecutor.getTaskId(), taskExecutor);
        CompletableFuture<Void> future = CompletableFuture.runAsync(taskExecutor, executorService);
        taskExecutor.setFuture(future);

        return taskExecutor.getTaskId();
    }

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

    public List<TaskResult> getTasks(TaskStage stage) {
        return tasks.values().stream()
                .filter(task -> task.getStage() == stage)
                .map(TaskResult::new)
                .collect(Collectors.toList());
    }

    public List<TaskResult> getTasks() {
        return tasks.values().stream()
                .map(TaskResult::new)
                .collect(Collectors.toList());
    }

    private TaskExecutor getTaskById(String taskId) {
        return tasks.computeIfAbsent(taskId, t -> {
            throw new NotFoundException("Task not found.");
        });
    }

    public TaskResultWidthLog getTaskResult(String id) {
        return new TaskResultWidthLog(getTaskById(id));
    }
}
