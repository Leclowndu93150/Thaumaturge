package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class TTBlockTags {
    public static final TagKey<Block> CRUCIBLE_HEAT_SOURCES = key("crucible_heat_sources");
    public static final TagKey<Block> SCAN_CLAY = key("scan/f_matclay");

    public static final TagKey<Block> PLANKS = common("planks");
    public static final TagKey<Block> PLANKS_GREATWOOD = common("planks/greatwood");
    public static final TagKey<Block> PLANKS_SILVERWOOD = common("planks/silverwood");

    public static final TagKey<Block> MAGICAL_PLANTS = key("magical_plants");
    public static final TagKey<Block> MAGICAL_FOREST_FLOWERS = key("magical_forest_flowers");
    public static final TagKey<Block> CINDERPEARL_SOIL = key("cinderpearl_soil");
    public static final TagKey<Block> SHIMMERLEAF_SOIL = key("shimmerleaf_soil");
    public static final TagKey<Block> VISHROOM_SOIL = key("vishroom_soil");

    public static final TagKey<Block> GREATWOOD_LOGS = key("greatwood_logs");
    public static final TagKey<Block> SILVERWOOD_LOGS = key("silverwood_logs");

    public static final TagKey<Block> ORES_AMBER = common("ores/amber");
    public static final TagKey<Block> ORES_CINNABAR = common("ores/cinnabar");

    public static final TagKey<Block> STORAGE_BLOCKS_BRASS = common("storage_blocks/brass");
    public static final TagKey<Block> STORAGE_BLOCKS_THAUMIUM = common("storage_blocks/thaumium");
    public static final TagKey<Block> STORAGE_BLOCKS_VOID_METAL = common("storage_blocks/void_metal");
    public static final TagKey<Block> STORAGE_BLOCKS_AMBER = common("storage_blocks/amber");

    public static final TagKey<Block> INFUSION_STABILISERS = key("infusion_stabilisers");

    public static final TagKey<Block> PORTABLE_HOLE_BLACKLIST = key("portable_hole_blacklist");

    public static final TagKey<Block> ELDRITCH_OBELISK_PARTS = key("eldritch_obelisk_parts");

    public static final TagKey<Block> LAMP_GROWTH_BLACKLIST = key("lamp_growth_blacklist");

    public static final TagKey<Block> ARCANE_WORKBENCH_CHARGER_HOSTS = key("arcane_workbench_charger_hosts");

    public static final TagKey<Block> WARDABLE_NON_SOLID = key("wardable_non_solid");
    public static final TagKey<Block> PHYSICAL_FLUX = key("physical_flux");
    public static final TagKey<Block> FLUX_SCRUBBABLE = key("flux_scrubbable");
    public static final TagKey<Block> MAGICAL_CAVE_GROUND_REPLACEABLE = key("magical_cave_ground_replaceable");
    public static final TagKey<Block> CANDLES = key("candles");
    public static final TagKey<Block> RESEARCH_BONUS_ORDO = key("research_bonus/ordo");
    public static final TagKey<Block> TAINT_CONVERTIBLE_LOG = key("taint_convertible/log");
    public static final TagKey<Block> TAINT_CONVERTIBLE_SOIL = key("taint_convertible/soil");
    public static final TagKey<Block> TAINT_CONVERTIBLE_ROCK = key("taint_convertible/rock");
    public static final TagKey<Block> TAINT_CONVERTIBLE_CRUST = key("taint_convertible/crust");
    public static final TagKey<Block> TAINT_CONVERSION_IMMUNE = key("taint_conversion_immune");
    public static final TagKey<Block> ARCANE_LOCKS = key("arcane_locks");

    private TTBlockTags() {}

    private static TagKey<Block> key(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TTIds.MODID, path));
    }

    private static TagKey<Block> common(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", path));
    }
}
