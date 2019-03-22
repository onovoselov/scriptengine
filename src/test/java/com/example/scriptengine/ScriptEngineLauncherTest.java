package com.example.scriptengine;

import com.example.scriptengine.config.ScriptEngineConfig;
import com.example.scriptengine.model.TaskLogArrayList;
import com.example.scriptengine.model.TaskLogList;
import com.example.scriptengine.service.script.EngineLauncher;
import com.example.scriptengine.service.script.ScriptLogWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ScriptEngineConfig.class)
public class ScriptEngineLauncherTest {
    private ScriptLogWriter writer = new ScriptLogWriter(new TaskLogArrayList());
    private TaskLogList logList;
    private Writer listStdout;

    @Autowired
    EngineLauncher engineLauncher;

    @Before
    public void setup() throws Exception {
        logList = new TaskLogArrayList();
        listStdout = new ScriptLogWriter(logList);
    }

    @Test
    public void testOkLaunch() throws IOException {
        String script = "print('Hello custom output writer');print('Hello2');";
        assertTrue(engineLauncher.launch(script, listStdout));
        assertEquals(logList.size(), 2);
        assertEquals(logList.get(0).getMessage(), "Hello custom output writer");
    }

    @Test
    public void testErrorLaunch() throws IOException {
        String script = "print777('Hello custom output writer');";
        assertFalse(engineLauncher.launch(script, listStdout));
        assertEquals(logList.size(), 1);
    }

    @Test
    public void testSleep2sec() throws IOException {
        assertTrue(engineLauncher.launch(Fixtures.scriptSleep3s, listStdout));
    }

}
