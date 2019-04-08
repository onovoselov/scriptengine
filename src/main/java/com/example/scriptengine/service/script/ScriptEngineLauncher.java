package com.example.scriptengine.service.script;

import com.example.scriptengine.exceptions.ScriptRuntimeException;
import com.example.scriptengine.exceptions.ThreadInterrupted;

import javax.script.*;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

/** Обертка над javax.script.ScriptEngine для запуска скрипта */
public class ScriptEngineLauncher implements EngineLauncher {
    private final ScriptEngine engine;
    private final String scriptBody;
    private final String scriptOwner;
    private CompiledScript compiledScript;

    public ScriptEngineLauncher(String scriptBody, String scriptOwner, ScriptEngine engine)
            throws ScriptRuntimeException {
        this.scriptBody = scriptBody;
        this.engine = engine;
        this.scriptOwner = scriptOwner;
        compile();
    }

    private void compile() throws ScriptRuntimeException {
        Compilable compilable = (Compilable) engine;
        try {
            compiledScript = compilable.compile(scriptBody);
        } catch (ScriptException e) {
            throw new ScriptRuntimeException(e.getMessage());
        }
    }

    /**
     * Выполнение скрипта
     *
     * @param stdoutWriter Writer для stdout скрипта
     * @return результат выполнения скрипта
     * @throws IOException если ошибка I/O
     */
    @Override
    public boolean launch(Writer stdoutWriter) throws IOException {
        ScriptContext context = new SimpleScriptContext();
        context.setWriter(stdoutWriter);

        try {
            compiledScript.eval(context);
        } catch (ThreadInterrupted e) {
            throw e;
        } catch (Throwable e) {
            stdoutWriter.write(
                    Optional.ofNullable(e.getMessage()).orElseThrow(ThreadInterrupted::new));
            return false;
        } finally {
            stdoutWriter.close();
        }

        return true;
    }

    @Override
    public String getScriptOwner() {
        return scriptOwner;
    }

    @Override
    public String getScriptBody() {
        return scriptBody;
    }
}
