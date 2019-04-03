package com.example.scriptengine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.problem.ProblemModule;
import org.zalando.problem.validation.ConstraintViolationProblemModule;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@Configuration
public class AppConfig {

    @Bean
    public ScriptEngine scriptEngine() {
        return new ScriptEngineManager().getEngineByName("Nashorn");
    }

    @Bean
    public ProblemModule problemModule() {
        return new ProblemModule();
    }

    /**
     * Stack trace On/Off
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new ProblemModule().withStackTraces(false));
    }

    @Bean
    public ConstraintViolationProblemModule constraintViolationProblemModule() {
        return new ConstraintViolationProblemModule();
    }
}
