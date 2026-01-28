package ru.slie.luna.plugins.gravity.script;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import java.util.function.Consumer;

public class ScriptLogger implements AutoCloseable {
    private final Appender appender;
    private final org.apache.logging.log4j.core.Logger logger;

    public ScriptLogger(String loggerName, Consumer<String> logConsumer) {
        String name = "ScriptAppender-" + System.currentTimeMillis();
        this.logger = (org.apache.logging.log4j.core.Logger) LogManager.getLogger(loggerName);

        this.appender = new AbstractAppender(name, null, null, true, Property.EMPTY_ARRAY) {
            @Override
            public void append(org.apache.logging.log4j.core.LogEvent event) {
                String msg = String.format("[%s] %s - %s",
                        event.getLevel(),
                        event.getLoggerName(),
                        event.getMessage().getFormattedMessage());
                logConsumer.accept(msg);
            }
        };

        appender.start();
        logger.addAppender(appender);
    }

    public Logger getlogger() {
        return logger;
    }

    @Override
    public void close() {
        appender.stop();
        logger.removeAppender(appender);
    }
}
