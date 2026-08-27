package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public final class TCBiomeTags {
    public static final TagKey<Item> CANDLES = item("candles");
    public static final TagKey<Block> INFUSION_STABILISERS = block("infusion_stabilisers");

    private static TagKey<Item> item(String path) {
        return TagKey.create(Registries.ITEM, TCIds.rl(path));
    }

    private static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, TCIds.rl(path));
    }

    public static final TagKey<Biome> HAS_GREATWOOD = biome("has_greatwood");
    public static final TagKey<Biome> HAS_GREATWOOD_RARE = biome("has_greatwood_rare");
    public static final TagKey<Biome> HAS_SILVERWOOD = biome("has_silverwood");
    public static final TagKey<Biome> HAS_CINDERPEARL = biome("has_cinderpearl");
    public static final TagKey<Biome> HAS_MOUND = biome("has_structure/mound");
    public static final TagKey<Biome> HAS_ELDRITCH_OBELISK = biome("has_structure/eldritch_obelisk");
    public static final TagKey<Biome> HAS_BRAINY_HUSK = biome("has_spawn/brainy_husk");
    public static final TagKey<Biome> IS_MAGICAL = biome("is_magical");
    public static final TagKey<Biome> IS_SPOOKY = biome("is_spooky");

    private static TagKey<Biome> biome(String path) {
        return TagKey.create(Registries.BIOME, TCIds.rl(path));
    }

    private TCBiomeTags() {}
}
