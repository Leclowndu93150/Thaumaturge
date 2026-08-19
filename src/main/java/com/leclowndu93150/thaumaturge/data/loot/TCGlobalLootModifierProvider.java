package com.leclowndu93150.thaumaturge.data.loot;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCLootTables;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

public final class TCGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public TCGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, TCIds.MODID);
    }

    @Override
    protected void start() {
        LootItemCondition[] treasureTargets = {anyOf(BuiltInLootTables.SIMPLE_DUNGEON, BuiltInLootTables.JUNGLE_TEMPLE, BuiltInLootTables.DESERT_PYRAMID, BuiltInLootTables.ABANDONED_MINESHAFT,
                BuiltInLootTables.STRONGHOLD_CORRIDOR, BuiltInLootTables.STRONGHOLD_CROSSING, BuiltInLootTables.STRONGHOLD_LIBRARY)};

        add("treasure_common", modifier(treasureTargets, TCLootTables.TREASURE_COMMON));
        add("treasure_uncommon", modifier(treasureTargets, TCLootTables.TREASURE_UNCOMMON));
        add("treasure_rare", modifier(treasureTargets, TCLootTables.TREASURE_RARE));

        add("library_knowledge", modifier(new LootItemCondition[]{anyOf(BuiltInLootTables.STRONGHOLD_LIBRARY)}, TCLootTables.TREASURE_LIBRARY));

        add("village_smith_quicksilver", modifier(new LootItemCondition[]{anyOf(BuiltInLootTables.VILLAGE_TOOLSMITH, BuiltInLootTables.VILLAGE_WEAPONSMITH)}, TCLootTables.TREASURE_SMITH));
    }

    private static AddTableLootModifier modifier(LootItemCondition[] conditions, ResourceKey<LootTable> table) {
        return new AddTableLootModifier(conditions, IGlobalLootModifier.DEFAULT_PRIORITY, table);
    }

    @SafeVarargs
    private static LootItemCondition anyOf(ResourceKey<LootTable>... targets) {
        AnyOfCondition.Builder builder = null;
        for (ResourceKey<LootTable> target : targets) {
            LootTableIdCondition.Builder condition = LootTableIdCondition.builder(target.identifier());
            builder = builder == null ? AnyOfCondition.anyOf(condition) : builder.or(condition);
        }
        return builder.build();
    }
}
