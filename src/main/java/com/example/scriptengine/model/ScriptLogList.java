package com.example.scriptengine.model;

import java.util.List;

public interface ScriptLogList {
    void add(ScriptLog scriptLog);

    int size();

    ScriptLog get(int pos);

    List<ScriptLog> getAndDeleteItems();
}
