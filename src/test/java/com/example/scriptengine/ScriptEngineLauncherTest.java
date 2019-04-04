package com.example.scriptengine;

import com.example.scriptengine.exceptions.ScriptCompileException;
import com.example.scriptengine.model.TaskLogArrayList;
import com.example.scriptengine.model.TaskLogList;
import com.example.scriptengine.service.script.EngineLauncher;
import com.example.scriptengine.service.script.ScriptEngineLauncher;
import com.example.scriptengine.service.script.writer.TaskLogWriter;
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
    final private static String USER_NAME = "TestUser";
    private TaskLogWriter writer = new TaskLogWriter(new TaskLogArrayList());
    private TaskLogList logList;
    private Writer listStdout;
    private EngineLauncher engineLauncher;
    private ScriptEngine engine;

    @Before
    public void setup() {
        logList = new TaskLogArrayList();
        listStdout = new TaskLogWriter(logList);
        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName("Nashorn");
    }

    @Test
    public void testOkLaunch() throws IOException, ScriptCompileException {
        String script = "print('Hello custom output writer');print('Hello2');";

        EngineLauncher engineLauncher = new ScriptEngineLauncher(script, USER_NAME, engine);
        assertTrue(engineLauncher.launch(listStdout));
        assertEquals(logList.size(), 2);
        assertEquals(logList.get(0).getMessage(), "Hello custom output writer");
    }

    @Test
    public void testErrorLaunch() throws IOException, ScriptCompileException {
        String script = "print777('Hello custom output writer');";
        EngineLauncher engineLauncher = new ScriptEngineLauncher(script, USER_NAME, engine);

        assertFalse(engineLauncher.launch(listStdout));
        assertEquals(logList.size(), 1);
    }
//
//    @Test
//    public void testSleep2sec() throws IOException {
//        assertTrue(engineLauncher.launch(Fixtures.scriptSleep3s, listStdout));
//    }

    @Test(expected = ScriptCompileException.class)
    public void testCompile() throws IOException, ScriptCompileException {
        EngineLauncher engineLauncher = new ScriptEngineLauncher(Fixtures.scriptError, USER_NAME, engine);
        engineLauncher.launch(listStdout);
    }

}
