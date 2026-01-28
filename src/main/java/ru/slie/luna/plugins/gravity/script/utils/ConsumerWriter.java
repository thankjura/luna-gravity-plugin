package ru.slie.luna.plugins.gravity.script.utils;

import org.jspecify.annotations.NonNull;

import java.io.Writer;
import java.util.function.Consumer;

public class ConsumerWriter extends Writer {
    private final Consumer<String> consumer;
    private final StringBuilder buffer = new StringBuilder();

    public ConsumerWriter(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void write(char @NonNull [] cbuf, int off, int len) {
        for (int i = 0; i < len; i++) {
            char c = cbuf[off + i];
            if (c == '\n') {
                flushBuffer();
            } else {
                buffer.append(c);
            }
        }
    }

    private void flushBuffer() {
        if (!buffer.isEmpty()) {
            consumer.accept(buffer.toString());
            buffer.setLength(0);
        }
    }

    @Override
    public void flush() {
        flushBuffer();
    }

    @Override
    public void close() {
        flushBuffer();
    }
}
