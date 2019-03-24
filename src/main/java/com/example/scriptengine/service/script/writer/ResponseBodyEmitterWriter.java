package com.example.scriptengine.service.script.writer;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.io.Writer;

/**
 * Writer для вывода в ResponseBodyEmitter
 */
public class ResponseBodyEmitterWriter extends Writer {

    final private ResponseBodyEmitter emitter;

    public ResponseBodyEmitterWriter(ResponseBodyEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (cbuf != null && len > 0 && cbuf[0] != '\r' && cbuf[0] != '\n') {
            emitter.send(String.valueOf(cbuf, off, len), MediaType.TEXT_PLAIN);
        }
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {
        emitter.complete();
    }
}
