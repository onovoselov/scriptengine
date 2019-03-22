package com.example.scriptengine;

import com.example.scriptengine.config.ScriptEngineConfig;
import com.example.scriptengine.model.Task;
import com.example.scriptengine.model.TaskStage;
import com.example.scriptengine.service.TaskService;
import com.example.scriptengine.service.script.EngineLauncher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ScriptEngineConfig.class)
public class TaskServiceTest {

    @Autowired
    EngineLauncher engineLauncher;

    @Test
    public void testInterrupt() throws InterruptedException {
        TaskService service = new TaskService(engineLauncher);
        service.run(new Task("1", Fixtures.scriptSleep3s), false);
        service.run(new Task("2", Fixtures.scriptSleep3s), false);
        service.run(new Task("3", Fixtures.scriptSleep3s), false);
        service.run(new Task("4", Fixtures.scriptSleep3s), false);

        assertEquals(service.getTasks(TaskStage.InProgress).size(), 4);
        TimeUnit.MILLISECONDS.sleep(500);
        assertTrue(service.interrupt("1"));
        assertTrue(service.interrupt("4"));
        TimeUnit.MILLISECONDS.sleep(500);
        assertEquals(service.getTasks(TaskStage.InProgress).size(), 2);
        assertEquals(service.getTasks(TaskStage.Interrupted).size(), 2);

        TimeUnit.SECONDS.sleep(3);
    }
}
