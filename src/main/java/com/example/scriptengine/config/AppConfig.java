package com.example.scriptengine.config;

import com.example.scriptengine.service.TaskService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@Configuration
public class AppConfig {
    @Bean
    public ScriptEngine scriptEngine() {
        return new ScriptEngineManager().getEngineByName("Nashorn");
    }
}
