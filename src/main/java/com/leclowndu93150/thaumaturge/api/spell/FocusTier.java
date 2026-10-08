package com.leclowndu93150.thaumaturge.api.spell;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

/**
 * The limits a focus item puts on the spell inscribed into it. Attached to focus items through the
 * {@link #DATA_MAP} data map; any item carrying it can hold a spell.
 *
 * @param complexity the complexity budget
 * @param depth      the most nodes on any path from the origin, origin excluded
 * @param branches   the most branching flow nodes
 * @param repeats    the most echo repeats summed over the spell
 * @since 1.0.0
 */
public record FocusTier(int complexity, int depth, int branches, int repeats) {
    /** Codec of a data map value. */
    public static final Codec<FocusTier> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.INT.fieldOf("complexity").forGetter(FocusTier::complexity),
                    Codec.INT.fieldOf("depth").forGetter(FocusTier::depth),
                    Codec.INT.optionalFieldOf("branches", 0).forGetter(FocusTier::branches),
                    Codec.INT.optionalFieldOf("repeats", 0).forGetter(FocusTier::repeats))
            .apply(i, FocusTier::new));

    /** The item data map {@code thaumaturge:spell_focus}, synced to clients. */
    public static final DataMapType<Item, FocusTier> DATA_MAP = DataMapType.builder(
                    TTIds.rl("spell_focus"), Registries.ITEM, CODEC)
            .synced(CODEC, false)
            .build();
}
