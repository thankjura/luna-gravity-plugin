package ru.slie.luna.plugins.gravity.script.groovy.completion;

import org.springframework.stereotype.Component;
import ru.slie.luna.plugins.gravity.script.groovy.Suggestion;
import ru.slie.luna.plugins.gravity.script.groovy.SuggestionKind;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

@Component
public class ClassPropertyProvider {
    public SuggestionsProviderResult getSuggestions(String term, Class<?> clazz, boolean isInstance, int limit) {
        SuggestionsProviderResult response = new SuggestionsProviderResult();

        for (Method method: clazz.getMethods()) {
            if (limit <= 0) {
                response.setIncomplete(true);
                return response;
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

            response.addSuggestion(Suggestion
                            .builder(method.getName(), SuggestionKind.Method)
                            .detail(method.getReturnType().getSimpleName())
                            .insertText(method.getName() + "()")
                            .doc(Arrays.toString(method.getParameterTypes()))
                            .build());
            limit--;
        }

        for (Field field: clazz.getFields()) {
            if (limit <=0) {
                response.setIncomplete(true);
                return response;
            }

            if (!Modifier.isPublic(field.getModifiers())) {
                continue;
            }

            if (!isInstance && !Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            if (term != null && !field.getName().toLowerCase().startsWith(term)) {
                continue;
            }

            response.addSuggestion(Suggestion
                                           .builder(field.getName(), SuggestionKind.Method)
                                           .detail(field.getType().getSimpleName())
                                           .insertText(field.getName())
                                           .build());
            limit--;
        }

        return response;
    }
}
