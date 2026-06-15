package ru.slie.luna.plugins.gravity.utils;

import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MetricsHelper {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();

    public static String stackTraceToString(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        throwable.printStackTrace(pw);
        return sw.toString();
    }

    public static String getPayloadString(Map<String, Object> scriptEnv) {
        if (scriptEnv == null || scriptEnv.isEmpty()) {
            return "{}";
        }

        try {
            Map<String, String> simplifiedMap = scriptEnv.entrySet().stream()
                                                        .collect(Collectors.toMap(
                                                                Map.Entry::getKey,
                                                                entry -> {
                                                                    Object val = entry.getValue();
                                                                    if (val == null) {
                                                                        return "null [null]";
                                                                    }
                                                                    return val + " [" + val.getClass().getSimpleName() + "]";
                                                                },
                                                                (existing, replacement) -> existing
                                                        ));

            return OBJECT_MAPPER.writeValueAsString(simplifiedMap);
        } catch (Exception e) {
            return "{\"error\": \"Failed to serialize env: " + e.getMessage() + "\"}";
        }
    }
}
