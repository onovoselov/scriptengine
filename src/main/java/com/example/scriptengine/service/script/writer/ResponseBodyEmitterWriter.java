package com.example.scriptengine.service.script.writer;

import com.example.scriptengine.exceptions.ThreadInterrupted;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.Writer;

/** Writer for ResponseBodyEmitter */
public class ResponseBodyEmitterWriter extends Writer {

    private final ResponseBodyEmitter emitter;

    public ResponseBodyEmitterWriter(ResponseBodyEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        try {
            emitter.send(String.valueOf(cbuf, off, len), MediaType.TEXT_PLAIN);
        } catch (Exception e) {
            throw new ThreadInterrupted("Interrupted by user");
        }
    }

    @Override
    public void flush() {}

    @Override
    public void close() {
        emitter.complete();
    }
}
