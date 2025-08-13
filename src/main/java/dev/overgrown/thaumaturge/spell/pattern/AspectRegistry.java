package dev.overgrown.thaumaturge.spell.pattern;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class AspectRegistry {
    private static final Map<Identifier, AspectEffect> REGISTRY = new HashMap<>();

    private AspectRegistry() {}

    public static void register(Identifier id, AspectEffect effect) {
        if (id == null || effect == null) return;
        REGISTRY.put(id, effect);
    }

    public static Optional<AspectEffect> get(Identifier id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(REGISTRY.get(id));
    }

    public static boolean contains(Identifier id) {
        return id != null && REGISTRY.containsKey(id);
    }

    public static void clear() {
        REGISTRY.clear();
    }
}
