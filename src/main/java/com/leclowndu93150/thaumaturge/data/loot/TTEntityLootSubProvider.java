package com.leclowndu93150.thaumaturge.data.loot;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.entity.loot.LabyrinthBoundCondition;
import com.leclowndu93150.thaumaturge.content.entity.loot.NoNearbyKinCondition;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SlimePredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class TTEntityLootSubProvider extends EntityLootSubProvider {
    private static final double GIANT_TAINTACLE_KIN_RADIUS = 48.0;
    private static final float CONSTRUCT_RARE_CHANCE = 0.2F;
    private static final float CONSTRUCT_COMMON_CHANCE = 0.5F;
    private static final float TURRET_BRASS_CHANCE = 0.3F;
    private static final float TURRET_IRON_CHANCE = 0.4F;
    private static final int CRYSTAL_SLIME_MIN_SIZE = 2;
    private static final float GUARDIAN_EYE_CHANCE = 0.05F;
    private static final float GUARDIAN_EYE_LOOTING_BONUS = 0.02F;
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
    protected boolean canHaveLootTable(EntityType<?> type) {
        return type == TTEntities.TURRET_CROSSBOW.get()
                || type == TTEntities.TURRET_CROSSBOW_ADVANCED.get()
                || type == TTEntities.ARCANE_BORE.get()
                || super.canHaveLootTable(type);
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
        add(TTEntities.FIRE_BAT.get(), LootTable.lootTable());
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
                        .withPool(playerKillPool(Items.ENDER_PEARL, CRAB_PEARL_CHANCE, CRAB_PEARL_LOOTING_BONUS)));
        add(TTEntities.INHABITED_ZOMBIE.get(), LootTable.lootTable());
        add(
                TTEntities.ELDRITCH_GUARDIAN.get(),
                LootTable.lootTable()
                        .withPool(playerKillPool(
                                TTItems.ELDRITCH_EYE.get(), GUARDIAN_EYE_CHANCE, GUARDIAN_EYE_LOOTING_BONUS)));
        add(TTEntities.CULTIST_PORTAL_LESSER.get(), LootTable.lootTable());
        add(TTEntities.ELDRITCH_WARDEN.get(), bossTable());
        add(TTEntities.ELDRITCH_GOLEM.get(), bossTable());
        add(TTEntities.CULTIST_LEADER.get(), LootTable.lootTable().withPool(singlePool(TTItems.LOOT_BAG_RARE.get())));
        add(
                TTEntities.CULTIST_PORTAL_GREATER.get(),
                LootTable.lootTable()
                        .withPool(singlePool(TTItems.PRIMORDIAL_PEARL.get()).when(unbound())));
        add(
                TTEntities.TAINTACLE_GIANT.get(),
                LootTable.lootTable()
                        .withPool(singlePool(TTItems.PRIMORDIAL_PEARL.get())
                                .when(unbound())
                                .when(NoNearbyKinCondition.noNearbyKin(GIANT_TAINTACLE_KIN_RADIUS))));
        add(
                TTEntities.TURRET_CROSSBOW.get(),
                LootTable.lootTable()
                        .withPool(chancePool(TTItems.MIND_CLOCKWORK.get(), CONSTRUCT_RARE_CHANCE))
                        .withPool(chancePool(TTItems.MECHANISM_SIMPLE.get(), CONSTRUCT_COMMON_CHANCE))
                        .withPool(chancePool(TTBlocks.PLANK_GREATWOOD.get(), CONSTRUCT_COMMON_CHANCE))
                        .withPool(chancePool(TTBlocks.PLANK_GREATWOOD.get(), CONSTRUCT_COMMON_CHANCE)));
        add(
                TTEntities.TURRET_CROSSBOW_ADVANCED.get(),
                LootTable.lootTable()
                        .withPool(chancePool(TTItems.MIND_BIOTHAUMIC.get(), CONSTRUCT_RARE_CHANCE))
                        .withPool(chancePool(TTItems.MECHANISM_SIMPLE.get(), CONSTRUCT_COMMON_CHANCE))
                        .withPool(chancePool(TTBlocks.PLANK_GREATWOOD.get(), CONSTRUCT_COMMON_CHANCE))
                        .withPool(chancePool(TTBlocks.PLANK_GREATWOOD.get(), CONSTRUCT_COMMON_CHANCE))
                        .withPool(chancePool(TTItems.PLATE_BRASS.get(), TURRET_BRASS_CHANCE))
                        .withPool(chancePool(TTItems.PLATE_IRON.get(), TURRET_IRON_CHANCE))
                        .withPool(chancePool(TTItems.PLATE_IRON.get(), TURRET_IRON_CHANCE)));
        add(
                TTEntities.ARCANE_BORE.get(),
                LootTable.lootTable()
                        .withPool(chancePool(TTItems.MIND_CLOCKWORK.get(), CONSTRUCT_RARE_CHANCE))
                        .withPool(chancePool(TTItems.MORPHIC_RESONATOR.get(), CONSTRUCT_RARE_CHANCE))
                        .withPool(chancePool(TTBlocks.CRYSTAL_AER.get(), CONSTRUCT_RARE_CHANCE))
                        .withPool(chancePool(TTBlocks.CRYSTAL_TERRA.get(), CONSTRUCT_RARE_CHANCE))
                        .withPool(chancePool(TTItems.MECHANISM_SIMPLE.get(), CONSTRUCT_COMMON_CHANCE))
                        .withPool(chancePool(TTItems.PLATE_BRASS.get(), CONSTRUCT_COMMON_CHANCE))
                        .withPool(chancePool(TTBlocks.PLANK_GREATWOOD.get(), CONSTRUCT_COMMON_CHANCE)));
        add(
                TTEntities.THAUMIC_SLIME.get(),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(vitiumCrystal())
                                .when(LootItemEntityPropertyCondition.hasProperties(
                                        LootContext.EntityTarget.THIS,
                                        EntityPredicate.Builder.entity()
                                                .subPredicate(SlimePredicate.sized(
                                                        MinMaxBounds.Ints.atLeast(CRYSTAL_SLIME_MIN_SIZE)))))));
    }

    private LootTable.Builder bossTable() {
        return LootTable.lootTable()
                .withPool(singlePool(TTItems.PRIMORDIAL_PEARL.get()).when(unbound()))
                .withPool(singlePool(TTItems.LOOT_BAG_RARE.get()).when(unbound()));
    }

    private static LootItemCondition.Builder unbound() {
        return InvertedLootItemCondition.invert(LabyrinthBoundCondition.labyrinthBound());
    }

    private static LootPool.Builder singlePool(ItemLike item) {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(item));
    }

    private static LootPool.Builder chancePool(ItemLike item, float chance) {
        return singlePool(item).when(LootItemRandomChanceCondition.randomChance(chance));
    }

    private LootItem.Builder<?> vitiumCrystal() {
        Holder<IAspect> vitium =
                this.registries.lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(TTAspects.VITIUM);
        return LootItem.lootTableItem(TTItems.ESSENTIA_CRYSTAL.get())
                .apply(SetComponentsFunction.setComponent(
                        TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(vitium, 1)));
    }

    private LootPool.Builder curioPool(ItemLike curio) {
        return playerKillPool(curio, CURIO_CHANCE, CURIO_LOOTING_BONUS);
    }

    private LootPool.Builder playerKillPool(ItemLike item, float chance, float lootingBonus) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item))
                .when(LootItemKilledByPlayerCondition.killedByPlayer())
                .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(
                        this.registries, chance, lootingBonus));
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
