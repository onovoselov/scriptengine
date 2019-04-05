package com.example.scriptengine;

import com.example.scriptengine.config.AppConfig;
import com.example.scriptengine.config.AppProperties;
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
@ContextConfiguration(classes = AppConfig.class)
public class TaskServiceTest {
    private static final String USER_NAME = "TestUser";
    private static final User user = new User(USER_NAME, "ROLE_ADMIN");

    @Autowired private ScriptEngine scriptEngine;

    private TaskService service;

    @Before
    public void setup() {
        service = new TaskService(scriptEngine, new AppProperties(10, 3000));
    }

    @Test
    public void testOkUnblocked()
            throws InterruptedException, ExecutionException, ScriptCompileException,
                    PermissionException {
        TaskExecutor task1 = service.runUnblocked(Fixtures.scriptSleep3s, USER_NAME);
        TaskExecutor task2 = service.runUnblocked(Fixtures.scriptSleep3s, USER_NAME);
        task1.getFuture().get();
        task2.getFuture().get();
        TaskResultWidthLog result = service.getTaskResult(task1.getTaskId(), user);
        assertEquals(result.getLog().size(), 2);
        result = service.getTaskResult(task1.getTaskId(), user);
        assertEquals(result.getLog().size(), 0);

        result = service.getTaskResult(task2.getTaskId(), user);
        assertEquals(result.getLog().size(), 2);
        result = service.getTaskResult(task2.getTaskId(), user);
        assertEquals(result.getLog().size(), 0);
    }

    @Test
    public void testOkBlocked()
            throws InterruptedException, ExecutionException, ScriptCompileException {
        StringWriter scriptWriter = new StringWriter();

        TaskExecutor task = service.runBlocked(Fixtures.scriptSleep3s, USER_NAME, scriptWriter);
        task.getFuture().get();

        String output = scriptWriter.toString();
        assertThat(output, CoreMatchers.containsString("Start sleep 3 sec"));
        assertThat(output, CoreMatchers.containsString("End sleep"));
    }

    @Test(timeout = 4000)
    public void testInterrupt()
            throws InterruptedException, ExecutionException, ScriptCompileException,
                    PermissionException {
        final CountDownLatch cdl = new CountDownLatch(1);

        Observer changeStageObserver =
                (o, arg) -> {
                    if (arg instanceof TaskStage) {
                        TaskStage stage = (TaskStage) arg;
                        if (stage == TaskStage.InProgress) {
                            cdl.countDown();
                        }
                    }
                };
        TaskExecutor task =
                service.runUnblocked(Fixtures.scriptSleep3s, USER_NAME, changeStageObserver);
        cdl.await();
        service.interrupt(task.getTaskId(), user);
        task.getFuture().get();
        assertEquals(task.getStage(), TaskStage.Interrupted);
    }

    @Test
    public void testScriptBody()
            throws ScriptCompileException, ExecutionException, InterruptedException,
                    PermissionException {
        TaskExecutor task = service.runUnblocked(Fixtures.script1, USER_NAME);
        task.getFuture().get();
        assertEquals(service.getTaskScriptBody(task.getTaskId(), user), Fixtures.script1);
    }

    @Test
    public void testScriptOutput()
            throws ScriptCompileException, ExecutionException, InterruptedException,
                    PermissionException {
        TaskExecutor task = service.runUnblocked(Fixtures.script1, USER_NAME);
        task.getFuture().get();
        assertThat(
                service.getTaskScriptOutput(task.getTaskId(), user),
                CoreMatchers.containsString("Hello ScriptEngine!!!!"));
    }
}
