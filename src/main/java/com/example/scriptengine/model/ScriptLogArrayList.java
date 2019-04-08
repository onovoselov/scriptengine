package com.example.scriptengine.model;

import java.util.ArrayList;
import java.util.List;

/** Реализация хранения списка ScriptLog в ArrayList */
public class ScriptLogArrayList implements ScriptLogList {
    private final List<ScriptLog> scriptLogList;

    public ScriptLogArrayList() {
        this.scriptLogList = new ArrayList<>();
    }

    @Override
    public synchronized void add(ScriptLog scriptLog) {
        scriptLogList.add(scriptLog);
    }

    @Override
    public synchronized int size() {
        return scriptLogList.size();
    }

    @Override
    public synchronized ScriptLog get(int pos) {
        return scriptLogList.get(pos);
    }

    @Override
    public synchronized List<ScriptLog> getAndDeleteItems() {
        List<ScriptLog> list = new ArrayList<>(scriptLogList);
        scriptLogList.clear();
        return list;
    }
}
