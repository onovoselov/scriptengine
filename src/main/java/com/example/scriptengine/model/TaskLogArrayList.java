package com.example.scriptengine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class TaskLogArrayList implements TaskLogList {
    final private List<TaskLog> taskLogList;

    public TaskLogArrayList() {
        this.taskLogList = new CopyOnWriteArrayList<>();
    }

    @Override
    public void add(TaskLog taskLog) {
        taskLogList.add(taskLog);
        System.out.println(taskLog.getMessage());
    }

    @Override
    public int size() {
        return taskLogList.size();
    }

    @Override
    public TaskLog get(int pos) {
        return taskLogList.get(pos);
    }

    @Override
    synchronized public List<TaskLog> getAndDeleteItems() {
        List<TaskLog> list = new ArrayList<>(taskLogList);
        taskLogList.clear();
        return list;
    }
}
