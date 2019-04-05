package com.example.scriptengine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class EngineRestControllerTest {
    private MockMvc mockMvc;

    @Autowired private WebApplicationContext webApplicationContext;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @WithMockUser(username = "user1", password = "111111")
    public void restAddBlockedTaskTest() throws Exception {
        mockMvc.perform(post("/task").contentType(MediaType.TEXT_HTML).content(Fixtures.script1))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user2", password = "222222")
    public void restAddUnblockedTaskTest() throws Exception {
        mockMvc.perform(
                        post("/task?blocked=0")
                                .contentType(MediaType.TEXT_HTML)
                                .content(Fixtures.script1))
                .andExpect(status().isCreated());
    }
}
