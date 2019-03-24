package com.example.scriptengine;

import com.example.scriptengine.model.TaskStage;
import com.example.scriptengine.model.dto.TaskResultWidthLog;
import com.example.scriptengine.service.TaskService;
import com.example.scriptengine.service.script.EngineLauncher;
import com.example.scriptengine.service.script.ScriptEngineLauncher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
public class TaskServiceTest {
    private EngineLauncher engineLauncher;

    @Before
    public void setup() throws Exception {
        engineLauncher = new ScriptEngineLauncher();
    }

    @Test
    public void testOkUnblocked() throws InterruptedException {
        TaskService service = new TaskService();
        String id1 = service.runUnblocked(Fixtures.scriptSleep3s, engineLauncher);
        TimeUnit.SECONDS.sleep(5);
        TaskResultWidthLog result = service.getTaskResult(id1);
        assertEquals(result.getLog().size(), 2);
        result = service.getTaskResult(id1);
        assertEquals(result.getLog().size(), 0);
    }

    @Test
    public void testInterrupt() throws InterruptedException {
        TaskService service = new TaskService();
        String id1 = service.runUnblocked(Fixtures.scriptSleep3s, engineLauncher);
        service.runUnblocked(Fixtures.scriptSleep3s, engineLauncher);
        service.runUnblocked(Fixtures.scriptSleep3s, engineLauncher);

        assertEquals(service.getTasks(TaskStage.InProgress).size(), 3);
        service.interrupt(id1);
        TimeUnit.MILLISECONDS.sleep(500);
        assertEquals(service.getTasks(TaskStage.InProgress).size(), 2);
        assertEquals(service.getTasks(TaskStage.Interrupted).size(), 1);

        TimeUnit.SECONDS.sleep(3);
    }
}
