package ru.slie.luna.plugins.gravity.script.groovy.index;

/**
 * Where an indexed class comes from. The order is the completion priority: lower rank is shown first.
 */
public enum ClassSource {
    /** luna-api: the public plugin/script API. */
    API(0),
    /** Most used JDK packages (java.util, java.time, java.util.stream...). */
    JDK_COMMON(1),
    /** Groovy runtime (groovy.lang, groovy.transform...). */
    GROOVY(2),
    /** Other java.* packages. */
    JDK(3),
    /** Bundled libraries (commons-lang3...). */
    LIBRARY(3),
    /** luna-core internals: suggested last and only after a couple of typed characters. */
    CORE(5);

    private final int rank;

    ClassSource(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }
}
