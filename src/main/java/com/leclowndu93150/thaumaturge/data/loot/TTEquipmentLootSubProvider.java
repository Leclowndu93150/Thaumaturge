package com.leclowndu93150.thaumaturge.data.loot;

import com.leclowndu93150.thaumaturge.content.entity.loot.DifficultyValue;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public final class TTEquipmentLootSubProvider implements LootTableSubProvider {
    private static final float CLERIC_BOOTS_CHANCE = 0.1F;
    private static final float CLERIC_BOOTS_CHANCE_HARD = 0.3F;
    private static final float KNIGHT_SPECIAL_CHANCE = 0.01F;
    private static final float KNIGHT_SPECIAL_CHANCE_HARD = 0.05F;
    private static final int KNIGHT_VOID_WEIGHT = 1;
    private static final int KNIGHT_THAUMIUM_BARE_WEIGHT = 2;
    private static final int KNIGHT_THAUMIUM_HELMED_WEIGHT = 2;
    private static final float ZOMBIE_GEAR_CHANCE = 0.6F;
    private static final float ZOMBIE_GEAR_CHANCE_HARD = 0.9F;

    public TTEquipmentLootSubProvider(HolderLookup.Provider registries) {}

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(
                TTLootTables.EQUIPMENT_CULTIST_CLERIC,
                withEach(
                                LootTable.lootTable(),
                                TTItems.CRIMSON_ROBE_HELM.get(),
                                TTItems.CRIMSON_ROBE_CHEST.get(),
                                TTItems.CRIMSON_ROBE_LEGS.get())
                        .withPool(chance(
                                TTItems.CRIMSON_BOOTS.get(), hardOr(CLERIC_BOOTS_CHANCE_HARD, CLERIC_BOOTS_CHANCE))));
        output.accept(
                TTLootTables.EQUIPMENT_CULTIST_KNIGHT,
                withEach(
                        LootTable.lootTable().withPool(knightArms()),
                        TTItems.CRIMSON_PLATE_CHEST.get(),
                        TTItems.CRIMSON_PLATE_LEGS.get(),
                        TTItems.CRIMSON_BOOTS.get()));
        output.accept(
                TTLootTables.EQUIPMENT_CULTIST_LEADER,
                withEach(
                                LootTable.lootTable(),
                                TTItems.CRIMSON_PRAETOR_HELM.get(),
                                TTItems.CRIMSON_PRAETOR_CHEST.get(),
                                TTItems.CRIMSON_PRAETOR_LEGS.get(),
                                TTItems.CRIMSON_BOOTS.get())
                        .withPool(single(AlternativesEntry.alternatives(
                                LootItem.lootTableItem(TTItems.VOID_SWORD.get())
                                        .when(LootItemRandomChanceCondition.randomChance(onlyOn(Difficulty.EASY))),
                                LootItem.lootTableItem(TTItems.CRIMSON_BLADE.get())))));
        NumberProvider zombieGear = hardOr(ZOMBIE_GEAR_CHANCE_HARD, ZOMBIE_GEAR_CHANCE);
        output.accept(
                TTLootTables.EQUIPMENT_INHABITED_ZOMBIE,
                withEach(LootTable.lootTable(), TTItems.CRIMSON_PLATE_HELM.get())
                        .withPool(chance(TTItems.CRIMSON_PLATE_CHEST.get(), zombieGear))
                        .withPool(chance(TTItems.CRIMSON_PLATE_LEGS.get(), zombieGear)));
    }

    private static LootPool.Builder knightArms() {
        LootTable special = LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(NestedLootTable.inlineLootTable(
                                        kit(TTItems.VOID_SWORD.get(), TTItems.CRIMSON_ROBE_HELM.get()))
                                .setWeight(KNIGHT_VOID_WEIGHT))
                        .add(LootItem.lootTableItem(TTItems.THAUMIUM_SWORD.get())
                                .setWeight(KNIGHT_THAUMIUM_BARE_WEIGHT))
                        .add(NestedLootTable.inlineLootTable(
                                        kit(TTItems.THAUMIUM_SWORD.get(), TTItems.CRIMSON_PLATE_HELM.get()))
                                .setWeight(KNIGHT_THAUMIUM_HELMED_WEIGHT)))
                .build();
        return single(AlternativesEntry.alternatives(
                NestedLootTable.inlineLootTable(special)
                        .when(LootItemRandomChanceCondition.randomChance(
                                hardOr(KNIGHT_SPECIAL_CHANCE_HARD, KNIGHT_SPECIAL_CHANCE))),
                NestedLootTable.inlineLootTable(kit(Items.IRON_SWORD, TTItems.CRIMSON_PLATE_HELM.get()))));
    }

    private static LootTable kit(ItemLike... items) {
        return withEach(LootTable.lootTable(), items).build();
    }

    private static LootTable.Builder withEach(LootTable.Builder table, ItemLike... items) {
        for (ItemLike item : items) {
            table.withPool(single(LootItem.lootTableItem(item)));
        }
        return table;
    }

    private static LootPool.Builder chance(ItemLike item, NumberProvider chance) {
        return single(LootItem.lootTableItem(item)).when(LootItemRandomChanceCondition.randomChance(chance));
    }

    private static LootPool.Builder single(LootPoolEntryContainer.Builder<?> entry) {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(entry);
    }

    private static NumberProvider hardOr(float hard, float otherwise) {
        return DifficultyValue.of(Map.of(Difficulty.HARD, hard), otherwise);
    }

    private static NumberProvider onlyOn(Difficulty difficulty) {
        return DifficultyValue.of(Map.of(difficulty, 1.0F), 0.0F);
    }
}
