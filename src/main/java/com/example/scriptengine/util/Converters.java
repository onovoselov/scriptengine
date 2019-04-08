package com.example.scriptengine.util;

import com.example.scriptengine.exceptions.NotFoundException;
import com.example.scriptengine.model.ScriptStage;

public class Converters {
    public static ScriptStage stringToScriptStage(String stage) {
        try {
            return ScriptStage.valueOf(stage);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException("Invalid stage name");
        }
    }
}
