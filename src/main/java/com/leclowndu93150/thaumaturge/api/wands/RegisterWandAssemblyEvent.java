package com.leclowndu93150.thaumaturge.api.wands;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;

/**
 * Collects assembly ingredients on the NeoForge event bus whenever the generated server data
 * pack is opened. Registries are bound at this point. Register descriptors synchronously in the
 * listener; do not retain the event. Registration is rebuilt for every reload, so configuration
 * changes and removed listeners cannot leave stale recipes. Duplicate keys fail explicitly.
 * Disabled descriptors remain registered but generate no recipes. Datapacks above the built-in
 * pack can override generated recipe identifiers through the normal resource priority rules.
 * Unknown registry keys fail pack generation, including keys in disabled descriptors. An addon
 * should omit its descriptor when the providing mod is absent, rather than substitute a key
 * from another namespace. Each part has exactly one descriptor; use a compound ingredient to
 * offer several acquisition items. Listeners may inspect the already registered base parts.
 *
 * <p>For example, a listener can register an ordinary item as a rod ingredient without extending
 * any Thaumaturge implementation class:
 * <pre>{@code
 * event.registerRod(new WandAssemblyPart<>(
 *         ResourceKey.create(WandRod.REGISTRY_KEY, Identifier.parse("addon:rod")),
 *         Ingredient.of(addonRodItem), List.of(), Set.of(WandAssemblyPart.Variant.WAND), true));
 * }</pre>
 *
 * <p>Generated addon recipe paths are {@code wand/assembly/external/<cap>/<rod>} or
 * {@code wand/sceptre/external/<cap>/<rod>}, where each part is its full namespaced identifier
 * encoded as lowercase UTF-8 hex. Base-only identifiers retain their existing paths. All gates
 * must pass: the rod's intrinsic gate (or base auromancy), both descriptors' gates, and sceptre
 * research for sceptres. Sceptres are never enabled implicitly for addon descriptors. Recipes
 * are synchronized by Minecraft; callbacks and descriptors are not sent over the network.
 *
 * @since 1.0.0
 */
public final class RegisterWandAssemblyEvent extends Event {
    private final Map<Identifier, WandAssemblyPart<WandRod>> rods = new LinkedHashMap<>();
    private final Map<Identifier, WandAssemblyPart<WandCap>> caps = new LinkedHashMap<>();
    private boolean closed;

    public void registerRod(WandAssemblyPart<WandRod> part) {
        if (!part.key().isFor(WandRod.REGISTRY_KEY)) {
            throw new IllegalArgumentException("Expected a wand rod registry key");
        }
        register(rods, part);
    }

    public void registerCap(WandAssemblyPart<WandCap> part) {
        if (!part.key().isFor(WandCap.REGISTRY_KEY)) {
            throw new IllegalArgumentException("Expected a wand cap registry key");
        }
        register(caps, part);
    }

    private <T> void register(Map<Identifier, WandAssemblyPart<T>> parts, WandAssemblyPart<T> part) {
        if (closed) {
            throw new IllegalStateException("Wand assembly registration has finished");
        }
        if (parts.putIfAbsent(part.key().identifier(), part) != null) {
            throw new IllegalArgumentException("Duplicate wand assembly part: " + part.key().identifier());
        }
    }

    public Map<Identifier, WandAssemblyPart<WandRod>> rods() {
        return Map.copyOf(rods);
    }

    public Map<Identifier, WandAssemblyPart<WandCap>> caps() {
        return Map.copyOf(caps);
    }

    public void close() {
        closed = true;
    }
}
