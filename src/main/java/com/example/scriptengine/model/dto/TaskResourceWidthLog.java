package com.example.scriptengine.model.dto;

import com.example.scriptengine.model.TaskLog;
import com.example.scriptengine.service.TaskExecutor;

import java.util.ArrayList;
import java.util.List;

/** Для вывода состояния и результатов работы скрипта */
public class TaskResourceWidthLog extends TaskResource {
    private List<TaskLog> output;

    public TaskResourceWidthLog(TaskExecutor task) {
        super(task);
        this.output = task.getTaskLogList().getAndDeleteItems();
    }

    public List<TaskLog> getOutput() {
        return output;
    }
}
