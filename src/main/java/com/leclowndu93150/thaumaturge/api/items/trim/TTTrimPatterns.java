package com.leclowndu93150.thaumaturge.api.items.trim;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.armortrim.TrimPattern;

/**
 * Public addon API for the armor trim patterns Thaumaturge registers. Constants are {@link ResourceKey}s into the
 * datapack trim pattern registry.
 *
 * <p>Unlike vanilla patterns, these also apply to Thaumaturge's modelled armors (the item tag
 * {@code thaumaturge:modelled_trimmable_armor}). A modelled armor draws its trim from
 * {@code <pattern namespace>:textures/trims/entity/<armor material path>/<pattern path>.png}, laid out on that
 * armor's own model UVs; an addon armor that ships such textures and adds them to the armor trims atlas gets the same
 * treatment.
 *
 * @since 1.1.0
 */
public final class TTTrimPatterns {
    public static final ResourceKey<TrimPattern> ARCANE = key("arcane");
    public static final ResourceKey<TrimPattern> OCCULT = key("occult");

    private TTTrimPatterns() {}

    private static ResourceKey<TrimPattern> key(String path) {
        return ResourceKey.create(Registries.TRIM_PATTERN, TTIds.rl(path));
    }
}
