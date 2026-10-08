package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.api.aspect.IAspectRecipeContributor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TTAspectContributors {
    private static final List<IAspectRecipeContributor> CONTRIBUTORS = new ArrayList<>();

    private TTAspectContributors() {}

    public static void register(List<IAspectRecipeContributor> contributors) {
        CONTRIBUTORS.addAll(contributors);
    }

    public static List<IAspectRecipeContributor> all() {
        return Collections.unmodifiableList(CONTRIBUTORS);
    }
}
