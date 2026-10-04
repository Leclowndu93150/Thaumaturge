package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public final class TCLootTables {
    public static final ResourceKey<LootTable> LOOT_BAG_COMMON = key("gameplay/loot_bag_common");
    public static final ResourceKey<LootTable> LOOT_BAG_UNCOMMON = key("gameplay/loot_bag_uncommon");
    public static final ResourceKey<LootTable> LOOT_BAG_RARE = key("gameplay/loot_bag_rare");
    public static final ResourceKey<LootTable> LORE_BOOK = key("gameplay/lore_book");

    public static final ResourceKey<LootTable> TAINTED_COW = key("entities/tainted/cow");
    public static final ResourceKey<LootTable> TAINTED_PIG = key("entities/tainted/pig");
    public static final ResourceKey<LootTable> TAINTED_CHICKEN = key("entities/tainted/chicken");
    public static final ResourceKey<LootTable> TAINTED_SHEEP = key("entities/tainted/sheep");
    public static final ResourceKey<LootTable> TAINTED_VILLAGER = key("entities/tainted/villager");
    public static final ResourceKey<LootTable> TAINTED_CREEPER = key("entities/tainted/creeper");

    public static final ResourceKey<LootTable> TREASURE_COMMON = key("chests/treasure_common");
    public static final ResourceKey<LootTable> TREASURE_UNCOMMON = key("chests/treasure_uncommon");
    public static final ResourceKey<LootTable> TREASURE_RARE = key("chests/treasure_rare");
    public static final ResourceKey<LootTable> TREASURE_LIBRARY = key("chests/treasure_library");
    public static final ResourceKey<LootTable> TREASURE_SMITH = key("chests/treasure_smith");

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, TCIds.rl(path));
    }

    private TCLootTables() {}
}
