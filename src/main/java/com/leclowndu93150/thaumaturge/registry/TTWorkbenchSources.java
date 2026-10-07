package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.api.recipe.IWorkbenchAuraSource;
import com.leclowndu93150.thaumaturge.api.recipe.IWorkbenchVisSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TTWorkbenchSources {
    private static final List<IWorkbenchVisSource> VIS_SOURCES = new ArrayList<>();
    private static final List<IWorkbenchAuraSource> AURA_SOURCES = new ArrayList<>();

    private TTWorkbenchSources() {}

    public static void registerVisSources(List<IWorkbenchVisSource> sources) {
        VIS_SOURCES.addAll(sources);
    }

    public static void registerAuraSources(List<IWorkbenchAuraSource> sources) {
        AURA_SOURCES.addAll(sources);
    }

    public static List<IWorkbenchVisSource> visSources() {
        return Collections.unmodifiableList(VIS_SOURCES);
    }

    public static List<IWorkbenchAuraSource> auraSources() {
        return Collections.unmodifiableList(AURA_SOURCES);
    }
}
