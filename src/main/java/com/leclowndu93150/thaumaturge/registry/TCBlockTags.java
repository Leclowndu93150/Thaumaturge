package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class TCBlockTags {
    public static final TagKey<Block> CRUCIBLE_HEAT_SOURCES = key("crucible_heat_sources");
    public static final TagKey<Block> SCAN_CLAY = key("scan/f_matclay");

    public static final TagKey<Block> GREATWOOD_LOGS = key("greatwood_logs");
    public static final TagKey<Block> SILVERWOOD_LOGS = key("silverwood_logs");

    public static final TagKey<Block> ORES_AMBER = common("ores/amber");
    public static final TagKey<Block> ORES_CINNABAR = common("ores/cinnabar");
    public static final TagKey<Block> ORES_QUARTZ = common("ores/quartz");

    public static final TagKey<Block> STORAGE_BLOCKS_BRASS = common("storage_blocks/brass");
    public static final TagKey<Block> STORAGE_BLOCKS_THAUMIUM = common("storage_blocks/thaumium");
    public static final TagKey<Block> STORAGE_BLOCKS_VOID_METAL = common("storage_blocks/void_metal");
    public static final TagKey<Block> STORAGE_BLOCKS_AMBER = common("storage_blocks/amber");

    public static final TagKey<Block> INFUSION_STABILISERS = key("infusion_stabilisers");

    public static final TagKey<Block> PORTABLE_HOLE_BLACKLIST = key("portable_hole_blacklist");

    public static final TagKey<Block> ELDRITCH_OBELISK_PARTS = key("eldritch_obelisk_parts");

    public static final TagKey<Block> LAMP_GROWTH_BLACKLIST = key("lamp_growth_blacklist");

    public static final TagKey<Block> MAGICAL_PLANTS = key("magical_plants");

    private TCBlockTags() {}

    private static TagKey<Block> key(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TCIds.MODID, path));
    }

    private static TagKey<Block> common(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
