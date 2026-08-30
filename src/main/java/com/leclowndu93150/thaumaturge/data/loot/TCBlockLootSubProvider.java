package com.leclowndu93150.thaumaturge.data.loot;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.manabean.BlockEntityManaPod;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.Set;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class TCBlockLootSubProvider extends BlockLootSubProvider {
    private static final float AMBER_CURIO_CHANCE = 0.1F;
    private static final float[] VENT_CURIO_CHANCES = {0.01F, 0.01F, 0.02F, 0.03F};

    private LootTable.Builder dropSelfWithoutExplosion(Block block) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(block)));
    }

    private LootTable.Builder crystalTable(BlockCrystal block) {
        Holder<IAspect> aspect = registries.lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(block.aspect());
        LootPoolSingletonContainer.Builder<?> entry = LootItem.lootTableItem(TCItems.ESSENTIA_CRYSTAL.get())
                .apply(SetComponentsFunction.setComponent(
                        TCDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1)));
        for (int size = 1; size <= 3; size++) {
            entry = entry.apply(SetItemCountFunction.setCount(ConstantValue.exactly(size + 1), false)
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(BlockCrystal.SIZE, size))));
        }
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(entry));
    }

    private static final float SECOND_BEAN_CHANCE = 0.67F;

    private LootTable.Builder manaPodTable(Block block) {
        LootItemBlockStatePropertyCondition.Builder[] grownStages =
                new LootItemBlockStatePropertyCondition.Builder[BlockEntityManaPod.MAX_AGE - 1];
        for (int age = 2; age <= BlockEntityManaPod.MAX_AGE; age++) {
            grownStages[age - 2] = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockManaPod.AGE, age));
        }
        return LootTable.lootTable()
                .withPool(this.applyExplosionCondition(
                        block,
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(AnyOfCondition.anyOf(grownStages))
                                .add(LootItem.lootTableItem(TCItems.MANA_BEAN.get())
                                        .apply(CopyComponentsFunction.copyComponents(
                                                        CopyComponentsFunction.Source.BLOCK_ENTITY)
                                                .include(TCDataComponents.CRYSTAL_ASPECT.get())))))
                .withPool(this.applyExplosionCondition(
                        block,
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(BlockManaPod.AGE, BlockEntityManaPod.MAX_AGE)))
                                .when(LootItemRandomChanceCondition.randomChance(SECOND_BEAN_CHANCE))
                                .add(LootItem.lootTableItem(TCItems.MANA_BEAN.get())
                                        .apply(CopyComponentsFunction.copyComponents(
                                                        CopyComponentsFunction.Source.BLOCK_ENTITY)
                                                .include(TCDataComponents.CRYSTAL_ASPECT.get())))));
    }

    private LootTable.Builder mirrorTable(Block block) {
        return LootTable.lootTable()
                .withPool(this.applyExplosionCondition(
                        block,
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(block)
                                        .apply(CopyComponentsFunction.copyComponents(
                                                        CopyComponentsFunction.Source.BLOCK_ENTITY)
                                                .include(TCDataComponents.MIRROR_LINK.get())))));
    }

    private LootTable.Builder bannerTable(ItemLike item) {
        return LootTable.lootTable()
                .withPool(this.applyExplosionCondition(
                        item,
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(item)
                                        .apply(CopyComponentsFunction.copyComponents(
                                                CopyComponentsFunction.Source.BLOCK_ENTITY)))));
    }

    private LootTable.Builder lootContainerTable(Block block, int rarity) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(this.hasSilkTouch())
                        .add(LootItem.lootTableItem(block)))
                .withPool(TreasureLootPools.treasurePool(
                                lookupProvider, rarity, UniformGenerator.between(1.0F + rarity, 3.0F + rarity))
                        .when(this.doesNotHaveSilkTouch()));
    }

    private final HolderLookup.Provider lookupProvider;

    public TCBlockLootSubProvider(HolderLookup.Provider lookupProvider) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, lookupProvider);
        this.lookupProvider = lookupProvider;
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return TCBlocks.BLOCKS.getEntries().stream()
                .map(holder -> (Block) holder.value())
                .toList();
    }

    @Override
    protected void generate() {
        dropSelf(TCBlocks.NODE_STABILIZER.get());
        dropSelf(TCBlocks.NODE_STABILIZER_ADVANCED.get());
        dropSelf(TCBlocks.NODE_TRANSDUCER.get());
        dropSelf(TCBlocks.VIS_RELAY.get());
        add(
                TCBlocks.JAR_NODE.get(),
                LootTable.lootTable()
                        .withPool(this.applyExplosionCondition(
                                TCBlocks.JAR_NODE.get(),
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(LootItem.lootTableItem(TCBlocks.JAR_NODE.get())
                                                .apply(CopyComponentsFunction.copyComponents(
                                                                CopyComponentsFunction.Source.BLOCK_ENTITY)
                                                        .include(TCDataComponents.NODE_DATA.get()))))));

        for (DyeColor dye : DyeColor.values()) {
            dropSelf(TCBlocks.CANDLES.get(dye).get());
            add(
                    TCBlocks.BANNERS.get(dye).get(),
                    bannerTable(TCItems.BANNERS.get(dye).get()));
            add(
                    TCBlocks.WALL_BANNERS.get(dye).get(),
                    bannerTable(TCItems.BANNERS.get(dye).get()));
        }
        add(TCBlocks.BANNER_CRIMSON_CULT.get(), bannerTable(TCItems.BANNER_CRIMSON_CULT.get()));
        add(TCBlocks.WALL_BANNER_CRIMSON_CULT.get(), bannerTable(TCItems.BANNER_CRIMSON_CULT.get()));
        generateResources();

        add(TCBlocks.LOOT_URN_COMMON.get(), lootContainerTable(TCBlocks.LOOT_URN_COMMON.get(), 0));
        add(TCBlocks.LOOT_URN_UNCOMMON.get(), lootContainerTable(TCBlocks.LOOT_URN_UNCOMMON.get(), 1));
        add(TCBlocks.LOOT_URN_RARE.get(), lootContainerTable(TCBlocks.LOOT_URN_RARE.get(), 2));
        add(TCBlocks.LOOT_CRATE_COMMON.get(), lootContainerTable(TCBlocks.LOOT_CRATE_COMMON.get(), 0));
        add(TCBlocks.LOOT_CRATE_UNCOMMON.get(), lootContainerTable(TCBlocks.LOOT_CRATE_UNCOMMON.get(), 1));
        add(TCBlocks.LOOT_CRATE_RARE.get(), lootContainerTable(TCBlocks.LOOT_CRATE_RARE.get(), 2));

        dropOther(TCBlocks.RESEARCH_TABLE.get(), TCBlocks.TABLE_WOOD.get());
        dropSelf(TCBlocks.DECONSTRUCTION_TABLE.get());
        dropSelf(TCBlocks.SPA.get());
        dropSelf(TCBlocks.FOCAL_MANIPULATOR.get());
        dropSelf(TCBlocks.ARCANE_WORKBENCH.get());
        dropSelf(TCBlocks.ARCANE_WORKBENCH_CHARGER.get());
        dropSelf(TCBlocks.CRUCIBLE.get());
        dropSelf(TCBlocks.ALEMBIC.get());
        dropSelf(TCBlocks.BELLOWS.get());
        dropSelf(TCBlocks.SMELTER_BASIC.get());
        dropSelf(TCBlocks.SMELTER_THAUMIUM.get());
        dropSelf(TCBlocks.SMELTER_VOID.get());
        dropSelf(TCBlocks.SMELTER_AUX.get());
        dropSelf(TCBlocks.SMELTER_VENT.get());
        add(TCBlocks.JAR_NORMAL.get(), jarLootTable(TCBlocks.JAR_NORMAL.get()));
        add(TCBlocks.JAR_VOID.get(), jarLootTable(TCBlocks.JAR_VOID.get()));
        dropSelf(TCBlocks.TUBE.get());
        dropSelf(TCBlocks.TUBE_VALVE.get());
        dropSelf(TCBlocks.TUBE_RESTRICT.get());
        dropSelf(TCBlocks.TUBE_FILTER.get());
        dropSelf(TCBlocks.TUBE_ONEWAY.get());
        dropSelf(TCBlocks.TUBE_BUFFER.get());

        for (DyeColor dye : DyeColor.values()) {
            dropSelf(TCBlocks.NITORS.get(dye).get());
        }
        add(TCBlocks.CRYSTAL_AER.get(), crystalTable(TCBlocks.CRYSTAL_AER.get()));
        add(TCBlocks.CRYSTAL_IGNIS.get(), crystalTable(TCBlocks.CRYSTAL_IGNIS.get()));
        add(TCBlocks.CRYSTAL_AQUA.get(), crystalTable(TCBlocks.CRYSTAL_AQUA.get()));
        add(TCBlocks.CRYSTAL_TERRA.get(), crystalTable(TCBlocks.CRYSTAL_TERRA.get()));
        add(TCBlocks.CRYSTAL_ORDO.get(), crystalTable(TCBlocks.CRYSTAL_ORDO.get()));
        add(TCBlocks.CRYSTAL_PERDITIO.get(), crystalTable(TCBlocks.CRYSTAL_PERDITIO.get()));
        add(TCBlocks.CRYSTAL_VITIUM.get(), crystalTable(TCBlocks.CRYSTAL_VITIUM.get()));
        dropSelf(TCBlocks.STONE_ARCANE.get());
        dropSelf(TCBlocks.STONE_ARCANE_BRICK.get());
        dropSelf(TCBlocks.STONE_ANCIENT.get());
        dropSelf(TCBlocks.STONE_ANCIENT_TILE.get());
        dropSelf(TCBlocks.STONE_ANCIENT_GLYPHED.get());
        dropSelf(TCBlocks.STONE_ELDRITCH_TILE.get());
        dropSelf(TCBlocks.STONE_POROUS.get());
        dropSelf(TCBlocks.STAIRS_ARCANE.get());
        dropSelf(TCBlocks.STAIRS_ARCANE_BRICK.get());
        dropSelf(TCBlocks.STAIRS_ANCIENT.get());
        add(TCBlocks.STONE_ANCIENT_ROCK.get(), noDrop());
        add(TCBlocks.STONE_ANCIENT_DOORWAY.get(), noDrop());
        dropSelf(TCBlocks.SAPLING_GREATWOOD.get());
        dropSelf(TCBlocks.SAPLING_SILVERWOOD.get());
        dropSelf(TCBlocks.LOG_GREATWOOD.get());
        dropSelf(TCBlocks.LOG_SILVERWOOD.get());
        dropSelf(TCBlocks.WOOD_GREATWOOD.get());
        dropSelf(TCBlocks.WOOD_SILVERWOOD.get());
        dropSelf(TCBlocks.STRIPPED_LOG_GREATWOOD.get());
        dropSelf(TCBlocks.STRIPPED_LOG_SILVERWOOD.get());
        dropSelf(TCBlocks.STRIPPED_WOOD_GREATWOOD.get());
        dropSelf(TCBlocks.STRIPPED_WOOD_SILVERWOOD.get());
        dropSelf(TCBlocks.PLANK_GREATWOOD.get());
        dropSelf(TCBlocks.PLANK_SILVERWOOD.get());
        add(
                TCBlocks.LEAVES_GREATWOOD.get(),
                createLeavesDrops(TCBlocks.LEAVES_GREATWOOD.get(), TCBlocks.SAPLING_GREATWOOD.get()));
        add(
                TCBlocks.LEAVES_SILVERWOOD.get(),
                createLeavesDrops(TCBlocks.LEAVES_SILVERWOOD.get(), TCBlocks.SAPLING_SILVERWOOD.get()));
        dropSelf(TCBlocks.PLANT_SHIMMERLEAF.get());
        dropSelf(TCBlocks.PLANT_CINDERPEARL.get());
        dropSelf(TCBlocks.PLANT_VISHROOM.get());
        add(TCBlocks.GRASS_AMBIENT.get(), block -> createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
        dropSelf(TCBlocks.TAINT_ROCK.get());
        dropSelf(TCBlocks.TAINT_SOIL.get());
        dropSelf(TCBlocks.TAINT_CRUST.get());
        dropSelf(TCBlocks.TAINT_GEYSER.get());
        dropSelf(TCBlocks.TAINT_LOG.get());
        dropSelf(TCBlocks.TAINT_FEATURE.get());
        add(TCBlocks.TAINT_FIBRE.get(), noDrop());
    }

    private LootTable.Builder createLeavesDrops(Block leaves, Block sapling) {
        return createLeavesDrops(leaves, sapling, NORMAL_LEAVES_SAPLING_CHANCES);
    }

    private LootTable.Builder jarLootTable(Block block) {
        return LootTable.lootTable()
                .withPool(applyExplosionCondition(
                        block,
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(block)
                                        .apply(CopyComponentsFunction.copyComponents(
                                                        CopyComponentsFunction.Source.BLOCK_ENTITY)
                                                .include(TCDataComponents.ESSENTIA_CONTENTS.get())
                                                .include(TCDataComponents.ASPECT_FILTER.get())))));
    }

    private void generateResources() {
        add(
                TCBlocks.ORE_AMBER.get(),
                b -> createOreDrop(b, TCItems.AMBER.get())
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(this.doesNotHaveSilkTouch())
                                .when(LootItemRandomChanceCondition.randomChance(AMBER_CURIO_CHANCE))
                                .add(LootItem.lootTableItem(TCItems.CURIO_PRESERVED.get()))));
        add(TCBlocks.MIRROR.get(), this::mirrorTable);
        add(TCBlocks.MIRROR_ESSENTIA.get(), this::mirrorTable);
        add(TCBlocks.MANA_POD.get(), this::manaPodTable);
        add(
                TCBlocks.ELDRITCH_CRAB_SPAWNER.get(),
                b -> LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(
                                        lookupProvider
                                                .lookupOrThrow(Registries.ENCHANTMENT)
                                                .getOrThrow(Enchantments.FORTUNE),
                                        VENT_CURIO_CHANCES))
                                .add(LootItem.lootTableItem(TCItems.CURIO_PRESERVED.get()))));
        dropSelf(TCBlocks.ORE_CINNABAR.get());
        add(TCBlocks.ORE_QUARTZ.get(), b -> createOreDrop(b, Items.QUARTZ));

        dropSelf(TCBlocks.ALCHEMICAL_CONSTRUCT.get());
        dropSelf(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());
        dropSelf(TCBlocks.INFUSION_MATRIX.get());

        dropSelf(TCBlocks.METAL_BRASS_BLOCK.get());
        dropSelf(TCBlocks.METAL_THAUMIUM_BLOCK.get());
        dropSelf(TCBlocks.METAL_VOID_BLOCK.get());
        dropSelf(TCBlocks.AMBER_BLOCK.get());
        dropOther(TCBlocks.NETHER_BRICKS_PLACEHOLDER.get(), Blocks.NETHER_BRICKS);
        dropOther(TCBlocks.OBSIDIAN_PLACEHOLDER.get(), Blocks.OBSIDIAN);
        dropSelf(TCBlocks.LEVITATOR.get());
        dropSelf(TCBlocks.POTION_SPRAYER.get());
        dropSelf(TCBlocks.PATTERN_CRAFTER.get());
        dropSelf(TCBlocks.INLAY.get());
        dropSelf(TCBlocks.DIOPTRA.get());
        dropSelf(TCBlocks.ARCANE_EAR.get());
        dropSelf(TCBlocks.ARCANE_EAR_TOGGLE.get());
        dropSelf(TCBlocks.LAMP_ARCANE.get());
        dropSelf(TCBlocks.LAMP_GROWTH.get());
        dropSelf(TCBlocks.LAMP_FERTILITY.get());
        dropSelf(TCBlocks.CENTRIFUGE.get());
        dropSelf(TCBlocks.ARCANE_BORE.get());
        dropSelf(TCBlocks.HUNGRY_CHEST.get());
        dropSelf(TCBlocks.EVERFULL_URN.get());
        dropSelf(TCBlocks.VIS_GENERATOR.get());
        dropSelf(TCBlocks.ESSENTIA_INPUT.get());
        dropSelf(TCBlocks.ESSENTIA_OUTPUT.get());
        dropSelf(TCBlocks.CONDENSER.get());
        dropSelf(TCBlocks.CONDENSER_LATTICE.get());
        dropSelf(TCBlocks.CONDENSER_LATTICE_DIRTY.get());
        dropSelf(TCBlocks.STABILIZER.get());
        dropSelf(TCBlocks.REDSTONE_RELAY.get());
        dropSelf(TCBlocks.ACTIVATOR_RAIL.get());
        add(TCBlocks.SLAB_GREATWOOD.get(), this::createSlabItemTable);
        add(TCBlocks.SLAB_SILVERWOOD.get(), this::createSlabItemTable);
        add(TCBlocks.SLAB_ARCANE_STONE.get(), this::createSlabItemTable);
        add(TCBlocks.SLAB_ARCANE_BRICK.get(), this::createSlabItemTable);
        add(TCBlocks.SLAB_ANCIENT.get(), this::createSlabItemTable);
        add(TCBlocks.SLAB_ELDRITCH.get(), this::createSlabItemTable);
        dropSelf(TCBlocks.STAIRS_GREATWOOD.get());
        dropSelf(TCBlocks.STAIRS_SILVERWOOD.get());
        dropSelf(TCBlocks.TABLE_WOOD.get());
        dropSelf(TCBlocks.TABLE_STONE.get());
        dropSelf(TCBlocks.PAVING_STONE_TRAVEL.get());
        dropSelf(TCBlocks.PAVING_STONE_BARRIER.get());
        dropSelf(TCBlocks.AMBER_BRICK.get());
        dropSelf(TCBlocks.FLESH_BLOCK.get());
        dropSelf(TCBlocks.OBSIDIAN_TILE.get());
        dropSelf(TCBlocks.OBSIDIAN_TOTEM.get());
        dropOther(TCBlocks.OBSIDIAN_TOTEM_CHARGED.get(), TCBlocks.OBSIDIAN_TOTEM.get());
        dropSelf(TCBlocks.ELDRITCH_STONE.get());
        dropSelf(TCBlocks.ELDRITCH_STONE_INERT.get());
        dropSelf(TCBlocks.ELDRITCH_ROCK.get());
        dropSelf(TCBlocks.ELDRITCH_CRUST.get());
        dropSelf(TCBlocks.ELDRITCH_CRUST_GLOWING.get());
        dropSelf(TCBlocks.STAIRS_ELDRITCH.get());
        dropSelf(TCBlocks.ELDRITCH_PEDESTAL.get());
        add(TCBlocks.ELDRITCH_STONE_CRYSTAL.get(), createSingleItemTable(TCItems.CURIO_KNOWLEDGE.get()));
        dropSelf(TCBlocks.ELDRITCH_DOOR.get());
        dropSelf(TCBlocks.VOID_SIPHON.get());
        dropOther(TCBlocks.THAUMATORIUM.get(), TCBlocks.ALCHEMICAL_CONSTRUCT.get());
        dropSelf(TCBlocks.BRAIN_BOX.get());
        dropSelf(TCBlocks.VIS_BATTERY.get());
        dropSelf(TCBlocks.MATRIX_SPEED.get());
        dropSelf(TCBlocks.MATRIX_COST.get());
        add(TCBlocks.JAR_BRAIN.get(), bannerTable(TCBlocks.JAR_BRAIN.get()));
        dropOther(TCBlocks.GOLEM_BUILDER.get(), Blocks.PISTON);
        dropSelf(TCBlocks.PEDESTAL_ARCANE.get());
        dropSelf(TCBlocks.RECHARGE_PEDESTAL.get());
        dropSelf(TCBlocks.PEDESTAL_ANCIENT.get());
        dropSelf(TCBlocks.PEDESTAL_ELDRITCH.get());
        dropSelf(TCBlocks.PILLAR_ARCANE.get());
        dropSelf(TCBlocks.PILLAR_ANCIENT.get());
        dropSelf(TCBlocks.PILLAR_ELDRITCH.get());
    }
}
