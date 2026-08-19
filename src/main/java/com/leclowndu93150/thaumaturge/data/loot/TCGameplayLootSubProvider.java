package com.leclowndu93150.thaumaturge.data.loot;

import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCLootTables;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class TCGameplayLootSubProvider implements LootTableSubProvider {
    private static final float BAG_MIN_ROLLS = 8.0F;
    private static final float BAG_MAX_ROLLS = 12.0F;

    private static final int COMMON_EMPTY_WEIGHT = 6;
    private static final int UNCOMMON_EMPTY_WEIGHT = 12;
    private static final int RARE_EMPTY_WEIGHT = 40;
    private static final int LIBRARY_EMPTY_WEIGHT = 1;
    private static final int SMITH_EMPTY_WEIGHT = 1;

    private final HolderLookup.Provider registries;

    public TCGameplayLootSubProvider(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(TCLootTables.LOOT_BAG_COMMON, bagTable(TreasureLootPools.COMMON));
        output.accept(TCLootTables.LOOT_BAG_UNCOMMON, bagTable(TreasureLootPools.UNCOMMON));
        output.accept(TCLootTables.LOOT_BAG_RARE, bagTable(TreasureLootPools.RARE));

        output.accept(TCLootTables.TREASURE_COMMON,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(EmptyLootItem.emptyItem().setWeight(COMMON_EMPTY_WEIGHT)).add(entry(TCItems.LOOT_BAG_COMMON, 2))
                                .add(entry(TCItems.QUICKSILVER, 2, 1.0F, 3.0F)).add(entry(TCItems.AMBER, 2, 1.0F, 3.0F)).add(entry(TCItems.NUGGET_QUICKSILVER, 2, 2.0F, 6.0F))
                                .add(entry(TCItems.SALIS_MUNDUS, 1, 1.0F, 2.0F))));

        output.accept(TCLootTables.TREASURE_UNCOMMON,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(EmptyLootItem.emptyItem().setWeight(UNCOMMON_EMPTY_WEIGHT)).add(entry(TCItems.LOOT_BAG_UNCOMMON, 2))
                                .add(entry(TCItems.AMULET_MUNDANE, 1)).add(entry(TCItems.RING_MUNDANE, 1)).add(entry(TCItems.GIRDLE_MUNDANE, 1)).add(entry(TCItems.CURIO_ARCANE, 2))
                                .add(entry(TCItems.CURIO_PRESERVED, 1))));

        output.accept(TCLootTables.TREASURE_RARE,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(EmptyLootItem.emptyItem().setWeight(RARE_EMPTY_WEIGHT)).add(entry(TCItems.LOOT_BAG_RARE, 3))
                                .add(entry(TCItems.THAUMONOMICON, 1)).add(entry(TCItems.THAUMIUM_SWORD, 1)).add(entry(TCItems.THAUMIUM_PICKAXE, 1)).add(entry(TCItems.THAUMIUM_AXE, 1))
                                .add(entry(TCItems.THAUMIUM_HOE, 1)).add(entry(TCItems.AMULET_FANCY, 1)).add(entry(TCItems.RING_FANCY, 1)).add(entry(TCItems.GIRDLE_FANCY, 1))
                                .add(entry(TCItems.RING_APPRENTICE, 1)).add(entry(TCItems.AMULET_VIS, 1)).add(entry(TCItems.CURIO_ANCIENT, 2))));

        output.accept(TCLootTables.TREASURE_LIBRARY, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(EmptyLootItem.emptyItem().setWeight(LIBRARY_EMPTY_WEIGHT)).add(entry(TCItems.CURIO_KNOWLEDGE, 3, 1.0F, 2.0F))));

        output.accept(TCLootTables.TREASURE_SMITH, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(EmptyLootItem.emptyItem().setWeight(SMITH_EMPTY_WEIGHT)).add(entry(TCItems.QUICKSILVER, 2, 1.0F, 3.0F))));
    }

    private static LootPoolSingletonContainer.Builder<?> entry(ItemLike item, int weight) {
        return LootItem.lootTableItem(item).setWeight(weight);
    }

    private static LootPoolSingletonContainer.Builder<?> entry(ItemLike item, int weight, float min, float max) {
        return LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }

    private LootTable.Builder bagTable(int rarity) {
        return LootTable.lootTable().withPool(TreasureLootPools.treasurePool(registries, rarity, UniformGenerator.between(BAG_MIN_ROLLS, BAG_MAX_ROLLS)));
    }
}
