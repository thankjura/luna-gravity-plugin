package ru.slie.luna.plugins.gravity.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHelper {
    public static String stackTraceToString(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
