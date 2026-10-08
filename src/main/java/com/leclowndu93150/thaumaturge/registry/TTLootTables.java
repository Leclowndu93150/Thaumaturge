package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public final class TTLootTables {
    public static final ResourceKey<LootTable> LOOT_BAG_COMMON = key("gameplay/loot_bag_common");
    public static final ResourceKey<LootTable> LOOT_BAG_UNCOMMON = key("gameplay/loot_bag_uncommon");
    public static final ResourceKey<LootTable> LOOT_BAG_RARE = key("gameplay/loot_bag_rare");
    public static final ResourceKey<LootTable> LORE_BOOK = key("gameplay/lore_book");
    public static final ResourceKey<LootTable> CHAMPION_BAG = key("gameplay/champion_bag");

    public static final ResourceKey<LootTable> EQUIPMENT_CULTIST_CLERIC = key("equipment/cultist_cleric");
    public static final ResourceKey<LootTable> EQUIPMENT_CULTIST_KNIGHT = key("equipment/cultist_knight");
    public static final ResourceKey<LootTable> EQUIPMENT_CULTIST_LEADER = key("equipment/cultist_leader");
    public static final ResourceKey<LootTable> EQUIPMENT_INHABITED_ZOMBIE = key("equipment/inhabited_zombie");

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

    public static final ResourceKey<LootTable> LABYRINTH_KEY_ROOM = key("labyrinth/reward/key_room");
    public static final ResourceKey<LootTable> LABYRINTH_WARDEN = key("labyrinth/reward/warden");
    public static final ResourceKey<LootTable> LABYRINTH_GOLEM = key("labyrinth/reward/golem");
    public static final ResourceKey<LootTable> LABYRINTH_CRIMSON_PORTAL = key("labyrinth/reward/crimson_portal");
    public static final ResourceKey<LootTable> LABYRINTH_TAINT_SWARM = key("labyrinth/reward/taint_swarm");
    public static final ResourceKey<LootTable> LABYRINTH_HIEROPHANT = key("labyrinth/reward/hierophant");
    public static final ResourceKey<LootTable> LABYRINTH_PRIMORDIAL_PEARL = key("labyrinth/primordial_pearl");

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, TTIds.rl(path));
    }

    private TTLootTables() {}
}
