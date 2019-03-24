package com.example.scriptengine;

import com.example.scriptengine.model.TaskStage;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
        mockMvc.perform(post("/task").
                contentType(MediaType.TEXT_HTML).content(Fixtures.script1)
        )
        .andExpect(status().isOk());
    }

    @Test
    public void restAddUnblockedTaskTest() throws Exception {
        mockMvc.perform(post("/task?blocked=0").
                contentType(MediaType.TEXT_HTML).content(Fixtures.script1)
        )
                .andExpect(status().isCreated());
    }
}
