package com.example.scriptengine;

import com.example.scriptengine.config.AppConfig;
import com.example.scriptengine.exceptions.PermissionException;
import com.example.scriptengine.exceptions.ScriptCompileException;
import com.example.scriptengine.model.TaskStage;
import com.example.scriptengine.model.User;
import com.example.scriptengine.model.dto.TaskResultWidthLog;
import com.example.scriptengine.service.TaskExecutor;
import com.example.scriptengine.service.TaskService;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.script.ScriptEngine;
import java.io.StringWriter;
import java.util.Observer;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= AppConfig.class)
public class TaskServiceTest {
    final private static String USER_NAME = "TestUser";
    final private static User user = new User(USER_NAME, "ROLE_ADMIN");


    @Autowired
    private ScriptEngine scriptEngine;

    private TaskService service;

    @Before
    public void setup() {
        service = new TaskService(scriptEngine);
    }

    @Test
    public void testOkUnblocked() throws InterruptedException, ExecutionException, ScriptCompileException, PermissionException {
        String id1 = service.runUnblocked(Fixtures.scriptSleep3s, USER_NAME);
        TaskExecutor task1 = service.getTaskById(id1);
        String id2 = service.runUnblocked(Fixtures.scriptSleep3s, USER_NAME);
        TaskExecutor task2 = service.getTaskById(id2);
        task1.getFuture().get();
        task2.getFuture().get();
        TaskResultWidthLog result = service.getTaskResult(id1, user);
        assertEquals(result.getLog().size(), 2);
        result = service.getTaskResult(id1, user);
        assertEquals(result.getLog().size(), 0);

        result = service.getTaskResult(id2, user);
        assertEquals(result.getLog().size(), 2);
        result = service.getTaskResult(id2, user);
        assertEquals(result.getLog().size(), 0);
    }

    @Test
    public void testOkBlocked() throws InterruptedException, ExecutionException, ScriptCompileException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();

        StringWriter scriptWriter = new StringWriter();
        Future<?> future = executor.submit(service.getTaskExecutor(Fixtures.scriptSleep3s, USER_NAME, scriptWriter));
        future.get();

        String output = scriptWriter.toString();
        assertThat(output, CoreMatchers.containsString("Start sleep 3 sec"));
        assertThat(output, CoreMatchers.containsString("End sleep"));
    }

    @Test(timeout=4000)
    public void testInterrupt() throws InterruptedException, ExecutionException, ScriptCompileException, PermissionException {
        final CountDownLatch cdl = new CountDownLatch(1);

        Observer changeStageObserver = (o, arg) -> {
            if(arg instanceof TaskStage) {
                TaskStage stage = (TaskStage) arg;
                if(stage == TaskStage.InProgress) {
                    cdl.countDown();
                }
            }
        };
        String id = service.runUnblocked(Fixtures.scriptSleep3s, USER_NAME, changeStageObserver);
        TaskExecutor task = service.getTaskById(id);

        cdl.await();
        service.interrupt(id, user);
        task.getFuture().get();
        assertEquals(task.getStage(), TaskStage.Interrupted);
    }

    @Test
    public void testScriptBody() throws ScriptCompileException, ExecutionException, InterruptedException, PermissionException {
        String id = service.runUnblocked(Fixtures.script1, USER_NAME);
        TaskExecutor task = service.getTaskById(id);
        task.getFuture().get();
        assertEquals(service.getTaskScriptBody(id, user), Fixtures.script1);
    }

    @Test
    public void testScriptOutput() throws ScriptCompileException, ExecutionException, InterruptedException, PermissionException {
        String id = service.runUnblocked(Fixtures.script1, USER_NAME);
        TaskExecutor task = service.getTaskById(id);
        task.getFuture().get();
        assertThat(service.getTaskScriptOutput(id, user), CoreMatchers.containsString("Hello ScriptEngine!!!!"));
    }
}
