package com.example.scriptengine.model;

/** Состояния в которых может находиться задача */
public enum ScriptStage {
    Pending,
    InProgress,
    DoneOk,
    DoneError,
    Interrupted,
}
