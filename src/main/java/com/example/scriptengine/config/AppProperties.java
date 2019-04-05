package com.example.scriptengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("app")
public class AppProperties {
    private int numThreads;
    private int interruptTimeout;

    public AppProperties() {
    }

    public AppProperties(int numThreads, int interruptTimeout) {
        this.numThreads = numThreads;
        this.interruptTimeout = interruptTimeout;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public int getInterruptTimeout() {
        return interruptTimeout;
    }

    public void setInterruptTimeout(int interruptTimeout) {
        this.interruptTimeout = interruptTimeout;
    }
}
