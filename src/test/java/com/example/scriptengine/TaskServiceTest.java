package com.example.scriptengine;

import com.example.scriptengine.model.TaskStage;
import com.example.scriptengine.model.dto.TaskResultWidthLog;
import com.example.scriptengine.service.TaskExecutor;
import com.example.scriptengine.service.TaskService;
import com.example.scriptengine.service.script.EngineLauncher;
import com.example.scriptengine.service.script.ScriptEngineLauncher;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.StringWriter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class TaskServiceTest {
    private EngineLauncher engineLauncher;

    @Before
    public void setup() throws Exception {
        engineLauncher = new ScriptEngineLauncher();
    }

    @Test
    public void testOkUnblocked() throws InterruptedException, ExecutionException {
        TaskService service = new TaskService();
        String id1 = service.runUnblocked(Fixtures.scriptSleep3s, engineLauncher);
        TaskExecutor task = service.getTaskById(id1);
        task.getFuture().get();
        TaskResultWidthLog result = service.getTaskResult(id1);
        assertEquals(result.getLog().size(), 2);
        result = service.getTaskResult(id1);
        assertEquals(result.getLog().size(), 0);
    }

    @Test
    public void testOkBlocked() throws InterruptedException, ExecutionException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();

        TaskService service = new TaskService();
        StringWriter scriptWriter = new StringWriter();
        Future<?> future = executor.submit(service.getTaskExecutor(Fixtures.scriptSleep3s, engineLauncher, scriptWriter));
        future.get();

        String output = scriptWriter.toString();
        assertThat(output, CoreMatchers.containsString("Start sleep 3 sec"));
        assertThat(output, CoreMatchers.containsString("End sleep"));
    }

    @Test
    public void testInterrupt() throws InterruptedException, ExecutionException {
        final Timer timer = new Timer();

        TaskService service = new TaskService();
        String id1 = service.runUnblocked(Fixtures.scriptSleep3s, engineLauncher);
        TaskExecutor task1 = service.getTaskById(id1);
        String id2 = service.runUnblocked(Fixtures.scriptSleep3s, engineLauncher);
        TaskExecutor task2 = service.getTaskById(id2);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                service.interrupt(id1);
            }
        }, 1000);

        task1.getFuture().get();
        task2.getFuture().get();
        assertEquals(task1.getStage(), TaskStage.Interrupted);
        assertEquals(task2.getStage(), TaskStage.DoneOk);
    }
}
