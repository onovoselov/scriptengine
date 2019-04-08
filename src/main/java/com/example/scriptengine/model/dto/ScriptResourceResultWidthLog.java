package com.example.scriptengine.model.dto;

import com.example.scriptengine.model.ScriptLog;
import com.example.scriptengine.service.ScriptExecutor;

import java.util.List;

/** Для вывода состояния и результатов работы скрипта */
public class ScriptResourceResultWidthLog extends ScriptResourceResult {
    private List<ScriptLog> output;

    public ScriptResourceResultWidthLog(ScriptExecutor scriptExecutor) {
        super(scriptExecutor);
        this.output = scriptExecutor.getScriptLogList().getAndDeleteItems();
    }

    public List<ScriptLog> getOutput() {
        return output;
    }
}
