package ru.slie.luna.plugins.gravity.script.groovy.completion;

import org.springframework.stereotype.Component;
import ru.slie.luna.plugins.gravity.script.groovy.Suggestion;
import ru.slie.luna.plugins.gravity.script.groovy.SuggestionKind;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ClassPropertyProvider {
    public List<Suggestion> getStaticSuggestions(String term, Class<?> clazz, boolean isInstance, int limit) {
        List<Suggestion> out = new ArrayList<>();
        for (Method method: clazz.getMethods()) {
            if (limit <= 0) {
                break;
            }

            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (!isInstance && !Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            if (term != null && !method.getName().toLowerCase().startsWith(term)) {
                continue;
            }

            out.add(Suggestion
                            .builder(method.getName(), SuggestionKind.Method)
                            .detail(method.getReturnType().getSimpleName())
                            .insertText(method.getName() + "()")
                            .doc(Arrays.toString(method.getParameterTypes()))
                            .build());
        }

        return out;
    }
}
