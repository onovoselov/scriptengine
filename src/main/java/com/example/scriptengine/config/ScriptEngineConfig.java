package com.example.scriptengine.config;

import com.example.scriptengine.service.script.EngineLauncher;
import com.example.scriptengine.service.script.ScriptEngineLauncher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ScriptEngineConfig {
    @Bean
    @Scope("singleton")
    EngineLauncher engineLauncher() {
        return new ScriptEngineLauncher();
    }
}
