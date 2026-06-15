package ru.slie.luna.plugins.gravity.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class SetUtils {
    private static final String DELIMITER = ",";

    @NonNull
    public static Set<Long> parseSet(String set) {
        Set<Long> out = new HashSet<>();
        if (set == null || set.isEmpty()) {
            return out;
        }

        for (String s: set.split(DELIMITER)) {
            if (NumberUtils.isCreatable(s.trim())) {
                out.add(Long.parseLong(s.trim()));
            }
        }

        return out;
    }

    @Nullable
    public static String setToString(Set<Long> set) {
        if (set == null || set.isEmpty()) {
            return null;
        }

        return StringUtils.join(set, DELIMITER);
    }
}
