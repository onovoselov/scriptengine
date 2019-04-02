package com.example.scriptengine;

import com.example.scriptengine.exceptions.ScriptCompileException;
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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class TaskServiceTest {


    @Before
    public void setup() throws Exception {
    }

    @Test
    public void testOkUnblocked() throws InterruptedException, ExecutionException, ScriptCompileException {
        TaskService service = new TaskService();
        String id1 = service.runUnblocked(Fixtures.scriptSleep3s);
        TaskExecutor task1 = service.getTaskById(id1);
        String id2 = service.runUnblocked(Fixtures.scriptSleep3s);
        TaskExecutor task2 = service.getTaskById(id2);
        task1.getFuture().get();
        task2.getFuture().get();
        TaskResultWidthLog result = service.getTaskResult(id1);
        assertEquals(result.getLog().size(), 2);
        result = service.getTaskResult(id1);
        assertEquals(result.getLog().size(), 0);

        result = service.getTaskResult(id2);
        assertEquals(result.getLog().size(), 2);
        result = service.getTaskResult(id2);
        assertEquals(result.getLog().size(), 0);
    }

    @Test
    public void testOkBlocked() throws InterruptedException, ExecutionException, ScriptCompileException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();

        TaskService service = new TaskService();
        StringWriter scriptWriter = new StringWriter();
        Future<?> future = executor.submit(service.getTaskExecutor(Fixtures.scriptSleep3s, scriptWriter));
        future.get();

        String output = scriptWriter.toString();
        assertThat(output, CoreMatchers.containsString("Start sleep 3 sec"));
        assertThat(output, CoreMatchers.containsString("End sleep"));
    }

    @Test(timeout=4000)
    public void testInterrupt() throws InterruptedException, ExecutionException, ScriptCompileException {
        final CountDownLatch cdl = new CountDownLatch(1);

        TaskService service = new TaskService();
        Observer changeStageObserver = (o, arg) -> {
            if(arg instanceof TaskStage) {
                TaskStage stage = (TaskStage) arg;
                if(stage == TaskStage.InProgress) {
                    cdl.countDown();
                }
            }
        };
        String id = service.runUnblocked(Fixtures.scriptSleep3s, changeStageObserver);
        TaskExecutor task = service.getTaskById(id);

        cdl.await();
        service.interrupt(id);
        task.getFuture().get();
        assertEquals(task.getStage(), TaskStage.Interrupted);
    }
}
