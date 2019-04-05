package com.example.scriptengine.model;

import java.util.List;

public interface TaskLogList {
    void add(TaskLog taskLog);

    int size();

    TaskLog get(int pos);

    List<TaskLog> getAndDeleteItems();
}
