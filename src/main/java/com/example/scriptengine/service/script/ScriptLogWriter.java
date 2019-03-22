package com.example.scriptengine.service.script;

import com.example.scriptengine.model.TaskLog;
import com.example.scriptengine.model.TaskLogList;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;

public class ScriptLogWriter extends Writer {
    final private TaskLogList logList;

    public ScriptLogWriter(TaskLogList logList) {
        this.logList = logList;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (cbuf != null && len > 0 && cbuf[0] != '\r' && cbuf[0] != '\n') {
            addTaskLog(String.valueOf(cbuf, off, len));
        }
    }

    private void addTaskLog(String message) {
        TaskLog taskLog = new TaskLog(LocalDateTime.now(), message);
        logList.add(taskLog);
    }

    public TaskLogList getLogList() {
        return logList;
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}
