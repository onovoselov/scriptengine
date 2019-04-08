package com.example.scriptengine;

import com.example.scriptengine.exceptions.ScriptRuntimeException;
import com.example.scriptengine.model.ScriptLogArrayList;
import com.example.scriptengine.model.ScriptLogList;
import com.example.scriptengine.service.script.EngineLauncher;
import com.example.scriptengine.service.script.ScriptEngineLauncher;
import com.example.scriptengine.service.script.writer.ScriptLogWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class ScriptEngineLauncherTest {
    private static final String USER_NAME = "TestUser";
    private ScriptLogList logList;
    private Writer listStdout;
    private ScriptEngine engine;

    @Before
    public void setup() {
        logList = new ScriptLogArrayList();
        listStdout = new ScriptLogWriter(logList);
        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName("Nashorn");
    }

    @Test
    public void testOkLaunch() throws IOException, ScriptRuntimeException {
        String script = "print('Hello custom output writer');print('Hello2');";

        EngineLauncher engineLauncher = new ScriptEngineLauncher(script, USER_NAME, engine);
        assertTrue(engineLauncher.launch(listStdout));
        assertEquals(logList.size(), 2);
        assertEquals(logList.get(0).getMessage(), "Hello custom output writer");
    }

    @Test
    public void testErrorLaunch() throws IOException, ScriptRuntimeException {
        String script = "print777('Hello custom output writer');";
        EngineLauncher engineLauncher = new ScriptEngineLauncher(script, USER_NAME, engine);

        assertFalse(engineLauncher.launch(listStdout));
        assertEquals(logList.size(), 1);
    }

    @Test(expected = ScriptRuntimeException.class)
    public void testCompile() throws IOException, ScriptRuntimeException {
        EngineLauncher engineLauncher =
                new ScriptEngineLauncher(Fixtures.scriptError, USER_NAME, engine);
        engineLauncher.launch(listStdout);
    }
}
