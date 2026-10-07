package com.leclowndu93150.thaumaturge.data.loot;

import com.leclowndu93150.thaumaturge.data.lang.LoreBookTextEn;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.SetBookCoverFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetWrittenBookPagesFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class TTGameplayLootSubProvider implements LootTableSubProvider {
    private static final float BAG_MIN_ROLLS = 8.0F;
    private static final float BAG_MAX_ROLLS = 12.0F;

    private static final int COMMON_EMPTY_WEIGHT = 6;
    private static final int UNCOMMON_EMPTY_WEIGHT = 12;
    private static final int RARE_EMPTY_WEIGHT = 40;
    private static final int LIBRARY_EMPTY_WEIGHT = 1;
    private static final int SMITH_EMPTY_WEIGHT = 1;

    private static final float LORE_CHANCE = 0.0005F;
    private static final String LORE_TITLE = "A Message to the World";
    private static final String LORE_AUTHOR = "A Thaumaturge";

    private final HolderLookup.Provider registries;

    public TTGameplayLootSubProvider(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(TTLootTables.LOOT_BAG_COMMON, bagTable(TreasureLootPools.COMMON));
        output.accept(TTLootTables.LOOT_BAG_UNCOMMON, bagTable(TreasureLootPools.UNCOMMON));
        output.accept(TTLootTables.LOOT_BAG_RARE, bagTable(TreasureLootPools.RARE).withPool(lorePool()));

        output.accept(TTLootTables.TREASURE_COMMON,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(EmptyLootItem.emptyItem().setWeight(COMMON_EMPTY_WEIGHT)).add(entry(TTItems.LOOT_BAG_COMMON, 2))
                                .add(entry(TTItems.QUICKSILVER, 2, 1.0F, 3.0F)).add(entry(TTItems.AMBER, 2, 1.0F, 3.0F)).add(entry(TTItems.NUGGET_QUICKSILVER, 2, 2.0F, 6.0F))
                                .add(entry(TTItems.SALIS_MUNDUS, 1, 1.0F, 2.0F))));

        output.accept(TTLootTables.TREASURE_UNCOMMON,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(EmptyLootItem.emptyItem().setWeight(UNCOMMON_EMPTY_WEIGHT)).add(entry(TTItems.LOOT_BAG_UNCOMMON, 2))
                                .add(entry(TTItems.AMULET_MUNDANE, 1)).add(entry(TTItems.RING_MUNDANE, 1)).add(entry(TTItems.GIRDLE_MUNDANE, 1)).add(entry(TTItems.CURIO_ARCANE, 2))
                                .add(entry(TTItems.CURIO_PRESERVED, 1))));

        output.accept(TTLootTables.TREASURE_RARE,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(EmptyLootItem.emptyItem().setWeight(RARE_EMPTY_WEIGHT)).add(entry(TTItems.LOOT_BAG_RARE, 3))
                                .add(entry(TTItems.THAUMONOMICON, 1)).add(entry(TTItems.THAUMIUM_SWORD, 1)).add(entry(TTItems.THAUMIUM_PICKAXE, 1)).add(entry(TTItems.THAUMIUM_AXE, 1))
                                .add(entry(TTItems.THAUMIUM_HOE, 1)).add(entry(TTItems.AMULET_FANCY, 1)).add(entry(TTItems.RING_FANCY, 1)).add(entry(TTItems.GIRDLE_FANCY, 1))
                                .add(entry(TTItems.RING_APPRENTICE, 1)).add(entry(TTItems.AMULET_VIS, 1)).add(entry(TTItems.CURIO_ANCIENT, 2)))
                        .withPool(lorePool()));

        output.accept(TTLootTables.TREASURE_LIBRARY,
                LootTable.lootTable().withPool(
                        LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(EmptyLootItem.emptyItem().setWeight(LIBRARY_EMPTY_WEIGHT)).add(entry(TTItems.CURIO_KNOWLEDGE, 3, 1.0F, 2.0F)))
                        .withPool(lorePool()));

        output.accept(TTLootTables.TREASURE_SMITH, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(EmptyLootItem.emptyItem().setWeight(SMITH_EMPTY_WEIGHT)).add(entry(TTItems.QUICKSILVER, 2, 1.0F, 3.0F))));

        output.accept(TTLootTables.LORE_BOOK,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(Items.WRITTEN_BOOK).apply(() -> new SetWrittenBookPagesFunction(List.of(), messagePages(), ListOperation.ReplaceAll.INSTANCE))
                                        .apply(() -> new SetBookCoverFunction(List.of(), Optional.of(Filterable.passThrough(LORE_TITLE)), Optional.of(LORE_AUTHOR), Optional.empty())))));
    }

    private static LootPool.Builder lorePool() {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(LORE_CHANCE)).add(NestedLootTable.lootTableReference(TTLootTables.LORE_BOOK));
    }

    private static List<Filterable<Component>> messagePages() {
        List<Filterable<Component>> pages = new ArrayList<>(LoreBookTextEn.pageCount());
        for (int page = 1; page <= LoreBookTextEn.pageCount(); page++) {
            pages.add(Filterable.passThrough(Component.translatable(LoreBookTextEn.pageKey(page))));
        }
        return pages;
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
