package com.example.scriptengine;

import com.example.scriptengine.model.dto.TaskStart;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class EngineRestControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void restAddBlockedTaskTest() throws Exception {
        TaskStart taskStart = new TaskStart(Fixtures.scriptSleep3s, true);
        String str = TestUtil.convertObjectToJsonString(taskStart);
        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonString(taskStart))
        )
                .andExpect(status().isOk());
    }

    @Test
    public void restAddUnblockedTaskTest() throws Exception {
        TaskStart taskStart = new TaskStart(Fixtures.script1, false);
        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonString(taskStart))
        )
                .andExpect(status().isCreated());
    }
}
