package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.Nullable;

public final class AspectCombinations {

    private AspectCombinations() {}

    public static @Nullable Holder<IAspect> result(HolderLookup.Provider registries, Holder<IAspect> first, Holder<IAspect> second) {
        for (Holder.Reference<IAspect> candidate : registries.lookupOrThrow(IAspect.REGISTRY_KEY).listElements().toList()) {
            List<Holder<IAspect>> components = candidate.value().components();
            if (components.size() != 2) {
                continue;
            }
            boolean direct = components.get(0).is(first.getKey()) && components.get(1).is(second.getKey());
            boolean swapped = components.get(0).is(second.getKey()) && components.get(1).is(first.getKey());
            if (direct || swapped) {
                return candidate;
            }
        }
        return null;
    }
}
