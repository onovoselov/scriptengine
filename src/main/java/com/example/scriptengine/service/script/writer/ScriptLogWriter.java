package com.example.scriptengine.service.script.writer;

import com.example.scriptengine.exceptions.ThreadInterrupted;
import com.example.scriptengine.model.ScriptLog;
import com.example.scriptengine.model.ScriptLogList;

import java.io.Writer;
import java.time.LocalDateTime;

/** Writer для вывода в ScriptLogList */
public class ScriptLogWriter extends Writer {
    private final ScriptLogList logList;
    private boolean closed;

    public ScriptLogWriter(ScriptLogList logList) {
        this.logList = logList;
        this.closed = false;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        if (closed) {
            throw new ThreadInterrupted("Interrupted by user");
        }

        if (cbuf != null && len > 0 && cbuf[0] != '\r' && cbuf[0] != '\n') {
            addScriptLog(String.valueOf(cbuf, off, len));
        }
    }

    private void addScriptLog(String message) {
        ScriptLog scriptLog = new ScriptLog(LocalDateTime.now(), message);
        logList.add(scriptLog);
    }

    public ScriptLogList getLogList() {
        return logList;
    }

    @Override
    public void flush() {}

    @Override
    public void close() {
        closed = true;
    }
}
