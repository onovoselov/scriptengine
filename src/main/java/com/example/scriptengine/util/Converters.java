package com.example.scriptengine.util;

import com.example.scriptengine.exceptions.NotFoundException;
import com.example.scriptengine.model.TaskStage;
import org.springframework.util.StringUtils;

public class Converters {
    public static TaskStage stringToTaskStage(String stage) {
        try {
            return TaskStage.valueOf(stage);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException();
        }
    }
}
