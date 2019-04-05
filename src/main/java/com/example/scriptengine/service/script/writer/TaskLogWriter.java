package com.example.scriptengine.service.script.writer;

import com.example.scriptengine.exceptions.ThreadInterrupted;
import com.example.scriptengine.model.TaskLog;
import com.example.scriptengine.model.TaskLogList;

import java.io.Writer;
import java.time.LocalDateTime;

/** Writer для вывода в TaskLogList */
public class TaskLogWriter extends Writer {
    private final TaskLogList logList;
    private boolean closed;

    public TaskLogWriter(TaskLogList logList) {
        this.logList = logList;
        this.closed = false;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        if (closed) {
            throw new ThreadInterrupted("Interrupted by user");
        }

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
    public void flush() {}

    @Override
    public void close() {
        closed = true;
    }
}
