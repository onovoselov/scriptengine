package com.example.scriptengine.service.script;

import com.example.scriptengine.exceptions.ThreadInterrupted;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

/**
 * Обертка над javax.script.ScriptEngine для запуска скрипта
 */
public class ScriptEngineLauncher implements EngineLauncher {
    private ScriptEngine engine;

    public ScriptEngineLauncher() {
        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName("JavaScript");
    }

    /**
     * Выполнение скрипта
     *
     * @param script тело скрипта
     * @param stdoutWriter Writer для stdout скрипта
     * @return результат выполнения скрипта
     * @throws IOException если ошибка I/O
     */
    @Override
    public boolean launch(String script, Writer stdoutWriter) throws IOException {
        ScriptContext context = engine.getContext();
        context.setWriter(stdoutWriter);

        try {
            engine.eval(script);
        } catch (Throwable e) {
            stdoutWriter.write( Optional.ofNullable(e.getMessage()).orElseThrow(ThreadInterrupted::new));
            return false;
        } finally {
            stdoutWriter.close();
        }

        return true;
    }
}
