package com.example.scriptengine.model.dto;

public class TaskLink {
    private String href;
    private String rel;

    public TaskLink(String href, String rel) {
        this.href = href;
        this.rel = rel;
    }

    public String getHref() {
        return href;
    }

    public String getRel() {
        return rel;
    }
}
