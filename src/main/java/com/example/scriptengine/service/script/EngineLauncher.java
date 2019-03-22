package com.example.scriptengine.service.script;

import java.io.IOException;
import java.io.Writer;

public interface EngineLauncher {
    boolean launch(String script, Writer stdoutWriter) throws IOException;
}
