package com.leclowndu93150.thaumaturge.data.loot;

import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCLootTables;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class TCTaintedLootSubProvider implements LootTableSubProvider {
    private static final float LIVESTOCK_CHANCE = 1.0F / 3.0F;
    private static final float VILLAGER_CHANCE = 0.5F;
    private static final float VILLAGER_COIN_CHANCE = 1.0F / 13.0F;
    private static final int CHICKEN_TENDRIL_WEIGHT = 3;

    private final HolderLookup.Provider registries;

    public TCTaintedLootSubProvider(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(TCLootTables.TAINTED_COW, LootTable.lootTable().withPool(taintPool(1)));
        output.accept(TCLootTables.TAINTED_CREEPER, LootTable.lootTable().withPool(taintPool(1)));
        output.accept(TCLootTables.TAINTED_PIG, LootTable.lootTable().withPool(taintPool(1).when(LootItemRandomChanceCondition.randomChance(LIVESTOCK_CHANCE))));
        output.accept(TCLootTables.TAINTED_SHEEP, LootTable.lootTable().withPool(taintPool(1).when(LootItemRandomChanceCondition.randomChance(LIVESTOCK_CHANCE))));
        output.accept(TCLootTables.TAINTED_CHICKEN, LootTable.lootTable().withPool(taintPool(CHICKEN_TENDRIL_WEIGHT)));
        output.accept(TCLootTables.TAINTED_VILLAGER,
                LootTable.lootTable().withPool(taintPool(1).when(LootItemRandomChanceCondition.randomChance(VILLAGER_CHANCE)))
                        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.GOLD_NUGGET))
                                .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(registries, VILLAGER_COIN_CHANCE, VILLAGER_COIN_CHANCE))));
    }

    private static LootPool.Builder taintPool(int tendrilWeight) {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(TCItems.TAINTED_GOO.get()))
                .add(LootItem.lootTableItem(TCItems.TAINT_TENDRIL.get()).setWeight(tendrilWeight));
    }
}
