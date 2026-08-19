package com.leclowndu93150.thaumaturge.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

/**
 * A single obtain or craft requirement on a {@link IResearchStage}.
 *
 * <p>The requirement matches any item from {@code items}, accepting either a tag reference or
 * one or more direct item references. The player must obtain or craft at least {@code amount}
 * matching items, counted in aggregate across the matched set.
 *
 * @param items items that satisfy this requirement
 * @param amount the minimum aggregate count, must be positive
 * @since 1.0.0
 */
public record ResearchRequirement(HolderSet<Item> items, DataComponentPatch components, int amount) {
    /** Codec for datapack serialization. */
    public static final Codec<ResearchRequirement> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(ResearchRequirement::items),
                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ResearchRequirement::components),
                    Codec.INT.fieldOf("amount").forGetter(ResearchRequirement::amount)).apply(instance, ResearchRequirement::new));
}
