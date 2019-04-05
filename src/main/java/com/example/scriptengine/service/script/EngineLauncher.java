package com.example.scriptengine.service.script;

import java.io.IOException;
import java.io.Writer;

public interface EngineLauncher {
    boolean launch(Writer stdoutWriter) throws IOException;

    String getScriptBody();

    String getScriptOwner();
}
