package com.leclowndu93150.thaumaturge.data.loot;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class TTEntityLootSubProvider extends EntityLootSubProvider {
    private static final float BRAIN_CHANCE = 0.5F;
    private static final float BRAIN_LOOTING_BONUS = 0.1F;
    private static final int GIANT_FLESH_ROLLS = 12;
    private static final float GIANT_FLESH_CHANCE = 0.5F;
    private static final float CRAB_PEARL_CHANCE = 0.33F;
    private static final float CRAB_PEARL_LOOTING_BONUS = 0.25F;
    private static final float CURIO_CHANCE = 0.0125F;
    private static final float CURIO_LOOTING_BONUS = 0.01F;

    public TTEntityLootSubProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), FeatureFlagSet.of(), registries);
    }

    @Override
    public void generate() {
        add(TTEntities.ELDRITCH_HIEROPHANT.get(), LootTable.lootTable());
        add(
                TTEntities.BRAINY_ZOMBIE.get(),
                LootTable.lootTable()
                        .withPool(fleshPool())
                        .withPool(zombieRareDropsPool())
                        .withPool(brainPool()));
        add(
                TTEntities.BRAINY_DROWNED.get(),
                LootTable.lootTable()
                        .withPool(fleshPool())
                        .withPool(zombieRareDropsPool())
                        .withPool(brainPool()));
        add(
                TTEntities.BRAINY_HUSK.get(),
                LootTable.lootTable()
                        .withPool(fleshPool())
                        .withPool(zombieRareDropsPool())
                        .withPool(brainPool()));
        add(TTEntities.MIND_SPIDER.get(), LootTable.lootTable());
        add(
                TTEntities.FIRE_BAT.get(),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(Items.GUNPOWDER)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
                                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                                this.registries, UniformGenerator.between(0.0F, 1.0F))))));
        add(
                TTEntities.GIANT_BRAINY_ZOMBIE.get(),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(GIANT_FLESH_ROLLS))
                                .add(LootItem.lootTableItem(Items.ROTTEN_FLESH)
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2)))
                                        .when(
                                                LootItemRandomChanceWithEnchantedBonusCondition
                                                        .randomChanceAndLootingBoost(
                                                                this.registries, GIANT_FLESH_CHANCE, 0.0F))))
                        .withPool(brainPool()));
        add(
                TTEntities.PECH.get(),
                LootTable.lootTable().withPool(goldNuggetPool()).withPool(curioPool(TTItems.CURIO_KNOWLEDGE.get())));
        add(
                TTEntities.CULTIST_KNIGHT.get(),
                LootTable.lootTable().withPool(goldNuggetPool()).withPool(curioPool(TTItems.CURIO_RITES.get())));
        add(
                TTEntities.CULTIST_CLERIC.get(),
                LootTable.lootTable().withPool(goldNuggetPool()).withPool(curioPool(TTItems.CURIO_RITES.get())));
        add(
                TTEntities.ELDRITCH_CRAB.get(),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(Items.ENDER_PEARL))
                                .when(LootItemKilledByPlayerCondition.killedByPlayer())
                                .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(
                                        this.registries, CRAB_PEARL_CHANCE, CRAB_PEARL_LOOTING_BONUS))));
        add(TTEntities.INHABITED_ZOMBIE.get(), LootTable.lootTable());
        add(TTEntities.ELDRITCH_GUARDIAN.get(), LootTable.lootTable());
        add(TTEntities.CULTIST_PORTAL_LESSER.get(), LootTable.lootTable());
    }

    private LootPool.Builder curioPool(ItemLike curio) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(curio))
                .when(LootItemKilledByPlayerCondition.killedByPlayer())
                .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(
                        this.registries, CURIO_CHANCE, CURIO_LOOTING_BONUS));
    }

    private LootPool.Builder goldNuggetPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.GOLD_NUGGET)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                this.registries, UniformGenerator.between(0.0F, 1.0F))));
    }

    private LootPool.Builder fleshPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.ROTTEN_FLESH)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                this.registries, UniformGenerator.between(0.0F, 1.0F))));
    }

    private LootPool.Builder zombieRareDropsPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.IRON_INGOT))
                .add(LootItem.lootTableItem(Items.CARROT))
                .add(LootItem.lootTableItem(Items.POTATO))
                .when(LootItemKilledByPlayerCondition.killedByPlayer())
                .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(
                        this.registries, 0.025F, 0.01F));
    }

    private LootPool.Builder brainPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(TTItems.BRAIN.get())
                        .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(
                                this.registries, BRAIN_CHANCE, BRAIN_LOOTING_BONUS)));
    }
}
