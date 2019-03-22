package com.example.scriptengine.service;

import com.example.scriptengine.exceptions.NotFoundException;
import com.example.scriptengine.model.Task;
import com.example.scriptengine.model.TaskStage;
import com.example.scriptengine.model.dto.TaskResult;
import com.example.scriptengine.model.dto.TaskResultWidthLog;
import com.example.scriptengine.service.script.EngineLauncher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class TaskService {
    final private static int NUM_TREADS = 10;
    final private ExecutorService executorService;
    final private Map<String, Task> tasks;
    private EngineLauncher engineLauncher;

    public TaskService(EngineLauncher engineLauncher) {
        this.engineLauncher = engineLauncher;
        this.executorService = Executors.newFixedThreadPool(NUM_TREADS);
        this.tasks = new ConcurrentHashMap<>();
    }

    public TaskResult run(Task task, boolean blocked) {
        TaskExecutor taskExecutor = new TaskExecutor(task, engineLauncher);
        CompletableFuture future = CompletableFuture.runAsync(taskExecutor, executorService);
        task.setFuture(future);
        tasks.put(task.getId(), task);

        if(blocked) {
            try {
                future.get();
            } catch (InterruptedException e) {
                task.interrupted();
            } catch (ExecutionException e) {
                task.error();
                e.printStackTrace();
            }

            return new TaskResultWidthLog(task);
        }

        return new TaskResult(task);
    }

    public TaskResult run(String script, boolean blocked) {
        Task task = new Task(script);
        return run(task, blocked);
    }

    public boolean interrupt(String taskId) {
        Thread thread = getThreadByName(taskId);
        if(thread != null) {
            thread.stop();
        }

        return true;
    }

    private Task getTaskById(String taskId) {
        return tasks.computeIfAbsent(taskId, t -> {
            throw new NotFoundException();
        });
    }

    public List<TaskResult> getTasks(TaskStage stage) {
        return tasks.values().stream()
                .filter(task -> task.getStage() == stage)
                .map(TaskResult::new)
                .collect(Collectors.toList());
    }

    private Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) {
                return t;
            }
        }

        return null;
    }

    public TaskResultWidthLog getTaskResult(String id) {
        return new TaskResultWidthLog(getTaskById(id));
    }
}
