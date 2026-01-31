package ru.slie.luna.plugins.gravity.script.groovy.completion;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import ru.slie.luna.plugins.gravity.script.groovy.Suggestion;
import ru.slie.luna.plugins.gravity.script.groovy.SuggestionKind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class LunaBeanProvider {
    private final ApplicationContext context;
    private final List<ClassWrapper> classesCache;

    public LunaBeanProvider(ApplicationContext context) {
        if (context.getParent() != null) {
            this.context = context.getParent();
        } else {
            this.context = context;
        }
        this.classesCache = new ArrayList<>();
    }

    private List<ClassWrapper> getBeans() {
        if (classesCache.isEmpty()) {
            String[] beanNames = context.getBeanDefinitionNames();
            ConfigurableListableBeanFactory factory = ((AnnotationConfigWebApplicationContext) context).getBeanFactory();

            for (String beanName: beanNames) {
                try {
                    String beanClass = factory.getBeanDefinition(beanName).getBeanClassName();
                    if (beanClass == null) {
                        continue;
                    }
                    Class<?> clazz = Class.forName(beanClass);
                    classesCache.add(new ClassWrapper(clazz));
                } catch (Exception ignored) {}
            }

        }

        return classesCache;
    }

    public SuggestionsProviderResult getSuggestions(String term, int limit) {
        List<ClassWrapper> classes = getBeans();

        if (term != null && !term.isEmpty()) {
            classes = classes.stream().filter(c -> c.like(term)).toList();
        }

        classes = classes.stream().sorted().limit(limit).toList();

        SuggestionsProviderResult out = new SuggestionsProviderResult();

        for (ClassWrapper clazz: classes) {
            if (limit <= 0) {
                out.setIncomplete(true);
                return out;
            }
            Suggestion suggestion = Suggestion.builder(clazz.getInsertText(), clazz.getKind())
                                            .detail(clazz.getCanonicalName())
                                            .addAutoImport(clazz.getCanonicalName()).build();
            out.addSuggestion(suggestion);
        }

        return out;
    }

    boolean isPublic(Class<?> clazz) {
        return clazz.getInterfaces().length > 0;
    }

    private class ClassWrapper implements Comparable<ClassWrapper> {
        private final Class<?> clazz;
        private final boolean pub;
        private final Class<?> face;

        private ClassWrapper(Class<?> clazz) {
            this.clazz = clazz;
            this.pub = isPublic(clazz);
            this.face = Arrays.stream(clazz.getInterfaces())
                                .filter(f -> f.getSimpleName()
                                                     .equals(clazz.getSimpleName()
                                                                     .replaceAll("Impl", "")))
                                .findFirst().orElse(null);
        }

        String getInsertText() {
            if (face != null) {
                return face.getSimpleName();
            }
            return clazz.getSimpleName();
        }

        String getCanonicalName() {
            if (face != null) {
                return face.getCanonicalName();
            }

            return clazz.getCanonicalName();
        }

        SuggestionKind getKind() {
            if (pub) {
                return SuggestionKind.Interface;
            }

            return SuggestionKind.Class;
        }

        private boolean like(@NonNull String term) {
            return  (clazz.getSimpleName().toLowerCase().startsWith(term));
        }

        @Override
        public int compareTo(@NonNull ClassWrapper wrapper) {
            return Boolean.compare(pub, wrapper.pub);
        }
    }
}
