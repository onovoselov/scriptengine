package com.example.scriptengine.model.dto;

import com.example.scriptengine.model.Task;
import com.example.scriptengine.model.TaskLog;

import java.util.ArrayList;
import java.util.List;

public class TaskResultWidthLog extends TaskResult {
    private List<TaskLog> log;

    public TaskResultWidthLog() {
        this.log = new ArrayList<>();
    }

    public TaskResultWidthLog(Task task) {
        super(task);
        this.log = task.getTaskLogList().getAndDeleteItems();
    }

    public List<TaskLog> getLog() {
        return log;
    }

    public void setLog(List<TaskLog> log) {
        this.log = log;
    }
}
