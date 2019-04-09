package com.example.scriptengine;

import com.example.scriptengine.config.AppConfig;
import com.example.scriptengine.config.AppProperties;
import com.example.scriptengine.exceptions.NotAcceptableException;
import com.example.scriptengine.exceptions.PermissionException;
import com.example.scriptengine.exceptions.ScriptRuntimeException;
import com.example.scriptengine.model.ScriptStage;
import com.example.scriptengine.model.User;
import com.example.scriptengine.model.dto.ScriptResourceResultWidthLog;
import com.example.scriptengine.service.ScriptExecutor;
import com.example.scriptengine.service.ScriptService;
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
public class ScriptServiceTest {
    private static final String USER_NAME = "TestUser";
    private static final User user = new User(USER_NAME, "ROLE_ADMIN");

    @Autowired private ScriptEngine scriptEngine;

    private ScriptService service;

    @Before
    public void setup() {
        service = new ScriptService(scriptEngine, new AppProperties(10, 3000));
    }

    @Test
    public void testOkUnblocked()
            throws InterruptedException, ExecutionException, ScriptRuntimeException,
                    PermissionException {
        ScriptExecutor script1 = service.runUnblocked(Fixtures.scriptSleep3s, USER_NAME);
        ScriptExecutor script2 = service.runUnblocked(Fixtures.scriptSleep3s, USER_NAME);
        script1.getFuture().get();
        script2.getFuture().get();
        ScriptResourceResultWidthLog result = service.getScriptResult(script1.getScriptId(), user);
        assertEquals(result.getOutput().size(), 2);
        result = service.getScriptResult(script1.getScriptId(), user);
        assertEquals(result.getOutput().size(), 0);

        result = service.getScriptResult(script2.getScriptId(), user);
        assertEquals(result.getOutput().size(), 2);
        result = service.getScriptResult(script2.getScriptId(), user);
        assertEquals(result.getOutput().size(), 0);
    }

    @Test
    public void testOkBlocked()
            throws InterruptedException, ExecutionException, ScriptRuntimeException {
        StringWriter scriptWriter = new StringWriter();

        ScriptExecutor scriptExecutor = service.runBlocked(Fixtures.scriptSleep3s, USER_NAME, scriptWriter);
        scriptExecutor.getFuture().get();

        String output = scriptWriter.toString();
        assertThat(output, CoreMatchers.containsString("Start sleep 3 sec"));
        assertThat(output, CoreMatchers.containsString("End sleep"));
    }

    @Test(timeout = 4000)
    public void testInterrupt()
        throws InterruptedException, ExecutionException, ScriptRuntimeException,
        PermissionException, NotAcceptableException {
        final CountDownLatch cdl = new CountDownLatch(1);

        Observer changeStageObserver =
                (o, arg) -> {
                    if (arg instanceof ScriptStage) {
                        ScriptStage stage = (ScriptStage) arg;
                        if (stage == ScriptStage.InProgress) {
                            cdl.countDown();
                        }
                    }
                };
        ScriptExecutor scriptExecutor =
                service.runUnblocked(Fixtures.scriptSleep3s, USER_NAME, changeStageObserver);
        cdl.await();
        service.interrupt(scriptExecutor.getScriptId(), user);
        scriptExecutor.getFuture().get();
        assertEquals(scriptExecutor.getStage(), ScriptStage.Interrupted);
    }

    @Test
    public void testScriptBody()
            throws ScriptRuntimeException, ExecutionException, InterruptedException,
                    PermissionException {
        ScriptExecutor scriptExecutor = service.runUnblocked(Fixtures.script1, USER_NAME);
        scriptExecutor.getFuture().get();
        assertEquals(service.getScriptBody(scriptExecutor.getScriptId(), user), Fixtures.script1);
    }

    @Test
    public void testScriptOutput()
            throws ScriptRuntimeException, ExecutionException, InterruptedException,
                    PermissionException {
        ScriptExecutor scriptExecutor = service.runUnblocked(Fixtures.script1, USER_NAME);
        scriptExecutor.getFuture().get();
        assertThat(
                service.getScriptOutput(scriptExecutor.getScriptId(), user),
                CoreMatchers.containsString("Hello ScriptEngine!!!!"));
    }

}
