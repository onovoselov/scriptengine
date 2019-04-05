package com.example.scriptengine.model;

/** Состояния в которых может находиться задача */
public enum TaskStage {
    Pending,
    InProgress,
    DoneOk,
    DoneError,
    Interrupted,
}
