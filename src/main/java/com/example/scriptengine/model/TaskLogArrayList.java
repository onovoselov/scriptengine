package com.example.scriptengine.model;

import java.util.ArrayList;
import java.util.List;

/** Реализация хранения списка TaskLog в ArrayList */
public class TaskLogArrayList implements TaskLogList {
    private final List<TaskLog> taskLogList;

    public TaskLogArrayList() {
        this.taskLogList = new ArrayList<>();
    }

    @Override
    public synchronized void add(TaskLog taskLog) {
        taskLogList.add(taskLog);
    }

    @Override
    public synchronized int size() {
        return taskLogList.size();
    }

    @Override
    public synchronized TaskLog get(int pos) {
        return taskLogList.get(pos);
    }

    @Override
    public synchronized List<TaskLog> getAndDeleteItems() {
        List<TaskLog> list = new ArrayList<>(taskLogList);
        taskLogList.clear();
        return list;
    }
}
