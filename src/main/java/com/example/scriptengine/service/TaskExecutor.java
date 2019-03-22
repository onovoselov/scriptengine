package com.example.scriptengine.service;

import com.example.scriptengine.exceptions.ThreadInterrupted;
import com.example.scriptengine.model.Task;
import com.example.scriptengine.service.script.EngineLauncher;
import com.example.scriptengine.service.script.ScriptLogWriter;

import java.io.IOException;

public class TaskExecutor implements Runnable {
    final private Task task;
    final private EngineLauncher engineLauncher;

    public TaskExecutor(Task task, EngineLauncher engineLauncher) {
        this.task = task;
        this.engineLauncher = engineLauncher;
    }

    @Override
    public void run() {
        final Thread currentThread = Thread.currentThread();
        currentThread.setName(task.getId());
        task.started();
        try {
            if(engineLauncher.launch(task.getContent(), new ScriptLogWriter(task.getTaskLogList()))) {
                task.stopped();
            } else {
                task.error();
            }
        } catch (ThreadInterrupted e) {
            task.interrupted();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
