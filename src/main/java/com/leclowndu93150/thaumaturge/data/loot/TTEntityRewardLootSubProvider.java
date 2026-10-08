package com.leclowndu93150.thaumaturge.data.loot;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class TTEntityRewardLootSubProvider implements LootTableSubProvider {
    private static final float CHAMPION_RARE_PER_LEVEL = 1.0F / 9.0F;
    private static final float CHAMPION_UNCOMMON_CHANCE = 4.0F / 9.0F;
    private static final List<Float> CHAMPION_UNCOMMON_BY_LEVEL =
            List.of(5.0F / 9.0F, 5.0F / 8.0F, 5.0F / 7.0F, 5.0F / 6.0F);

    private final HolderLookup.Provider registries;

    public TTEntityRewardLootSubProvider(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(TTLootTables.CHAMPION_BAG, championBag());
    }

    private LootTable.Builder championBag() {
        Holder<Enchantment> looting =
                registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING);
        LootItemCondition.Builder rare = () -> new LootItemRandomChanceWithEnchantedBonusCondition(
                0.0F, LevelBasedValue.perLevel(0.0F, CHAMPION_RARE_PER_LEVEL), looting);
        LootItemCondition.Builder uncommon = () -> new LootItemRandomChanceWithEnchantedBonusCondition(
                CHAMPION_UNCOMMON_CHANCE,
                LevelBasedValue.lookup(CHAMPION_UNCOMMON_BY_LEVEL, LevelBasedValue.constant(1.0F)),
                looting);
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(AlternativesEntry.alternatives(
                                LootItem.lootTableItem(TTItems.LOOT_BAG_RARE.get())
                                        .when(rare),
                                LootItem.lootTableItem(TTItems.LOOT_BAG_UNCOMMON.get())
                                        .when(uncommon),
                                LootItem.lootTableItem(TTItems.LOOT_BAG_COMMON.get()))));
    }
}
