package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class TCItemTags {
    public static final TagKey<Item> WANDS = key("wands");
    public static final TagKey<Item> RUNIC_SHIELDABLE = key("runic_shieldable");
    public static final TagKey<Item> SCAN_IRON = key("scan/f_matiron");
    public static final TagKey<Item> NITORS = key("nitors");
    public static final TagKey<Item> MEAT_CHUNKS = key("meat_chunks");

    public static final TagKey<Item> GREATWOOD_LOGS = key("greatwood_logs");
    public static final TagKey<Item> SILVERWOOD_LOGS = key("silverwood_logs");

    public static final TagKey<Item> RARE_EARTH_CHANCE_LOW = key("rare_earth_chance_low");
    public static final TagKey<Item> RARE_EARTH_CHANCE_NORMAL = key("rare_earth_chance_normal");
    public static final TagKey<Item> RARE_EARTH_CHANCE_HIGH = key("rare_earth_chance_high");
    public static final TagKey<Item> CLUSTERS = key("clusters");

    public static final TagKey<Item> INGOTS_TIN = common("ingots/tin");
    public static final TagKey<Item> INGOTS_LEAD = common("ingots/lead");
    public static final TagKey<Item> INGOTS_SILVER = common("ingots/silver");
    public static final TagKey<Item> INGOTS_BRASS = common("ingots/brass");
    public static final TagKey<Item> INGOTS_THAUMIUM = common("ingots/thaumium");
    public static final TagKey<Item> INGOTS_VOID_METAL = common("ingots/void_metal");

    public static final TagKey<Item> GEMS_QUICKSILVER = common("gems/quicksilver");
    public static final TagKey<Item> GEMS_AMBER = common("gems/amber");

    public static final TagKey<Item> NUGGETS_BRASS = common("nuggets/brass");
    public static final TagKey<Item> NUGGETS_THAUMIUM = common("nuggets/thaumium");
    public static final TagKey<Item> NUGGETS_VOID_METAL = common("nuggets/void_metal");
    public static final TagKey<Item> NUGGETS_QUICKSILVER = common("nuggets/quicksilver");
    public static final TagKey<Item> NUGGETS_QUARTZ = common("nuggets/quartz");
    public static final TagKey<Item> NUGGETS_TIN = common("nuggets/tin");
    public static final TagKey<Item> NUGGETS_COPPER = common("nuggets/copper");
    public static final TagKey<Item> NUGGETS_LEAD = common("nuggets/lead");
    public static final TagKey<Item> NUGGETS_SILVER = common("nuggets/silver");

    public static final TagKey<Item> PLATES = common("plates");
    public static final TagKey<Item> PLATES_IRON = common("plates/iron");
    public static final TagKey<Item> PLATES_BRASS = common("plates/brass");
    public static final TagKey<Item> PLATES_THAUMIUM = common("plates/thaumium");
    public static final TagKey<Item> PLATES_VOID_METAL = common("plates/void_metal");

    public static final TagKey<Item> ORES_TIN = common("ores/tin");
    public static final TagKey<Item> ORES_LEAD = common("ores/lead");
    public static final TagKey<Item> ORES_SILVER = common("ores/silver");
    public static final TagKey<Item> ORES_AMBER = common("ores/amber");
    public static final TagKey<Item> ORES_CINNABAR = common("ores/cinnabar");
    public static final TagKey<Item> ORES_QUARTZ = common("ores/quartz");

    public static final TagKey<Item> STORAGE_BLOCKS_BRASS = common("storage_blocks/brass");
    public static final TagKey<Item> STORAGE_BLOCKS_THAUMIUM = common("storage_blocks/thaumium");
    public static final TagKey<Item> STORAGE_BLOCKS_VOID_METAL = common("storage_blocks/void_metal");
    public static final TagKey<Item> STORAGE_BLOCKS_AMBER = common("storage_blocks/amber");

    public static final TagKey<Item> CANDLES = key("candles");

    public static final TagKey<Item> MAGICAL_PLANTS = key("magical_plants");

    public static final TagKey<Item> ARMORS_HELMETS = common("armors/helmets");
    public static final TagKey<Item> ARMORS_CHESTPLATES = common("armors/chestplates");
    public static final TagKey<Item> ARMORS_LEGGINGS = common("armors/leggings");
    public static final TagKey<Item> ARMORS_BOOTS = common("armors/boots");

    private TCItemTags() {}

    private static TagKey<Item> key(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(TCIds.MODID, path));
    }

    private static TagKey<Item> common(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
