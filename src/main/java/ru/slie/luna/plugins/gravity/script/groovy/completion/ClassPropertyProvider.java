package ru.slie.luna.plugins.gravity.script.groovy.completion;

import org.springframework.stereotype.Component;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.script.groovy.model.InsertTextRule;
import ru.slie.luna.plugins.gravity.script.groovy.model.Suggestion;
import ru.slie.luna.plugins.gravity.script.groovy.model.SuggestionKind;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ClassPropertyProvider {
    private final I18nResolver i18n;

    public ClassPropertyProvider(I18nResolver i18n) {
        this.i18n = i18n;
    }

    Map<String, List<Method>> getGroupedMethods(Class<?> clazz, boolean isInstance) {
        Map<String, List<Method>> out = new HashMap<>();

        for (Method method: clazz.getMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (!isInstance && !Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            if (out.containsKey(method.getName())) {
                out.get(method.getName()).add(method);
            } else {
                out.put(method.getName(), new ArrayList<>(List.of(method)));
            }
        }

        return out;
    }

    public SuggestionsProviderResult getSuggestions(String term, Class<?> clazz, boolean isInstance, int limit) {
        SuggestionsProviderResult response = new SuggestionsProviderResult();

        for (Map.Entry<String, List<Method>> entry: getGroupedMethods(clazz, isInstance).entrySet()) {
            if (limit <= 0) {
                response.setIncomplete(true);
                return response;
            }

            if (term != null && !entry.getKey().toLowerCase().startsWith(term)) {
                continue;
            }

            String details = "";
            String docs = "";
            if (entry.getValue().size() == 1) {
                details = entry.getValue().getFirst().getReturnType().getSimpleName();
                docs = Arrays.toString(entry.getValue().getFirst().getParameterTypes());
            } else {
                details = i18n.getText("gravity.script.method_overloads", String.valueOf(entry.getValue().size()));
                docs = entry.getValue().stream()
                        .map(m -> m.getName() + Arrays.toString(m.getParameterTypes()))
                        .collect(Collectors.joining("\n"));
            }

            Suggestion.Builder builder = Suggestion.builder(entry.getKey(), SuggestionKind.Method).detail(details).doc(docs);
            if (entry.getValue().getFirst().getParameterTypes().length == 0) {
                builder.insertText(entry.getKey() + "()");
            } else {
                StringJoiner stringJoiner = new StringJoiner(", ");
                for (int i = 1; i < entry.getValue().getFirst().getParameterTypes().length + 1; i++) {
                    stringJoiner.add(String.format("${%s:arg%s}", i, i));
                }

                builder.insertText(String.format("%s(%s)", entry.getKey(), stringJoiner))
                       .insertTextRule(InsertTextRule.InsertAsSnippet);
            }

            response.addSuggestion(builder.build());
            limit--;
        }

        for (Field field: clazz.getFields()) {
            if (limit <=0) {
                response.setIncomplete(true);
                return response;
            }



            if (term != null && !field.getName().toLowerCase().startsWith(term)) {
                continue;
            }

            response.addSuggestion(Suggestion
                                           .builder(field.getName(), SuggestionKind.Field)
                                           .detail(field.getType().getSimpleName())
                                           .insertText(field.getName())
                                           .build());
            limit--;
        }

        return response;
    }
}
