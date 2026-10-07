package com.leclowndu93150.thaumaturge.data.loot;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.decor.BlockCandleHolder;
import com.leclowndu93150.thaumaturge.content.decor.HeldCandle;
import com.leclowndu93150.thaumaturge.content.manabean.BlockEntityManaPod;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.Set;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
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
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class TTBlockLootSubProvider extends BlockLootSubProvider {
    private static final float[] TAINT_ROCK_CRYSTAL_CHANCES = {1 / 15.0F, 2 / 15.0F, 3 / 15.0F, 4 / 15.0F, 5 / 15.0F, 6 / 15.0F, 7 / 15.0F, 8 / 15.0F, 9 / 15.0F, 10 / 15.0F, 11 / 15.0F, 12 / 15.0F,
            13 / 15.0F, 14 / 15.0F, 15 / 15.0F};
    private static final float AMBER_CURIO_CHANCE = 0.1F;
    private static final float[] VENT_CURIO_CHANCES = {0.01F, 0.01F, 0.02F, 0.03F};

    private LootTable.Builder dropSelfWithoutExplosion(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block)));
    }

    private LootTable.Builder taintRockTable(Block block) {
        Holder<IAspect> aspect = lookupProvider.lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(TTAspects.VITIUM);
        LootItem.Builder<?> crystal = LootItem.lootTableItem(TTItems.ESSENTIA_CRYSTAL.get())
                .apply(SetComponentsFunction.setComponent(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1)));
        return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block))))
                .withPool(applyExplosionCondition(block,
                        LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(doesNotHaveSilkTouch())
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(lookupProvider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), TAINT_ROCK_CRYSTAL_CHANCES))
                                .add(crystal)));
    }

    private LootTable.Builder candleHolderTable(Block holder) {
        LootTable.Builder table = LootTable.lootTable().withPool(this.applyExplosionCondition(holder, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(holder))));
        for (HeldCandle held : HeldCandle.values()) {
            if (!held.isPresent()) {
                continue;
            }
            Item candle = TTItems.CANDLES.get(held.dye().orElseThrow()).get();
            table.withPool(this.applyExplosionCondition(holder, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(candle)).when(
                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(holder).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockCandleHolder.CANDLE, held)))));
        }
        return table;
    }

    private LootTable.Builder crystalTable(BlockCrystal block) {
        Holder<IAspect> aspect = lookupProvider.lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(block.aspect());
        LootPoolSingletonContainer.Builder<?> entry = LootItem.lootTableItem(TTItems.ESSENTIA_CRYSTAL.get())
                .apply(SetComponentsFunction.setComponent(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1)));
        for (int size = 1; size <= 3; size++) {
            entry = entry.apply(SetItemCountFunction.setCount(ConstantValue.exactly(size + 1), false)
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockCrystal.SIZE, size))));
        }
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(entry));
    }

    private static final float SECOND_BEAN_CHANCE = 0.67F;

    private LootTable.Builder manaPodTable(Block block) {
        LootItemBlockStatePropertyCondition.Builder[] grownStages = new LootItemBlockStatePropertyCondition.Builder[BlockEntityManaPod.MAX_AGE - 1];
        for (int age = 2; age <= BlockEntityManaPod.MAX_AGE; age++) {
            grownStages[age - 2] = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockManaPod.AGE, age));
        }
        return LootTable.lootTable()
                .withPool(this.applyExplosionCondition(block,
                        LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(AnyOfCondition.anyOf(grownStages))
                                .add(LootItem.lootTableItem(TTItems.MANA_BEAN.get())
                                        .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(TTDataComponents.CRYSTAL_ASPECT.get())))))
                .withPool(this.applyExplosionCondition(block,
                        LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockManaPod.AGE, BlockEntityManaPod.MAX_AGE)))
                                .when(LootItemRandomChanceCondition.randomChance(SECOND_BEAN_CHANCE)).add(LootItem.lootTableItem(TTItems.MANA_BEAN.get())
                                        .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(TTDataComponents.CRYSTAL_ASPECT.get())))));
    }

    private LootTable.Builder mirrorTable(Block block) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(block).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(TTDataComponents.MIRROR_LINK.get())))));
    }

    private LootTable.Builder bannerTable(ItemLike item) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition(item,
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(item).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)))));
    }

    private LootTable.Builder lootContainerTable(Block block, int rarity) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(this.hasSilkTouch()).add(LootItem.lootTableItem(block)))
                .withPool(TreasureLootPools.treasurePool(lookupProvider, rarity, UniformGenerator.between(1.0F + rarity, 3.0F + rarity)).when(this.doesNotHaveSilkTouch()));
    }

    private final HolderLookup.Provider lookupProvider;

    public TTBlockLootSubProvider(HolderLookup.Provider lookupProvider) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, lookupProvider);
        this.lookupProvider = lookupProvider;
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return TTBlocks.BLOCKS.getEntries().stream().map(holder -> (Block) holder.value()).toList();
    }

    @Override
    protected void generate() {
        add(TTBlocks.POTTED_SAPLING_GREATWOOD.get(), createPotFlowerItemTable(TTBlocks.SAPLING_GREATWOOD.get()));
        add(TTBlocks.POTTED_SAPLING_SILVERWOOD.get(), createPotFlowerItemTable(TTBlocks.SAPLING_SILVERWOOD.get()));
        add(TTBlocks.POTTED_SHIMMERLEAF.get(), createPotFlowerItemTable(TTBlocks.PLANT_SHIMMERLEAF.get()));
        add(TTBlocks.POTTED_CINDERPEARL.get(), createPotFlowerItemTable(TTBlocks.PLANT_CINDERPEARL.get()));
        add(TTBlocks.POTTED_VISHROOM.get(), createPotFlowerItemTable(TTBlocks.PLANT_VISHROOM.get()));

        dropSelf(TTBlocks.NODE_STABILIZER.get());
        dropSelf(TTBlocks.NODE_STABILIZER_ADVANCED.get());
        dropSelf(TTBlocks.NODE_TRANSDUCER.get());
        dropSelf(TTBlocks.VIS_RELAY.get());
        add(TTBlocks.JAR_NODE.get(), LootTable.lootTable().withPool(this.applyExplosionCondition(TTBlocks.JAR_NODE.get(), LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem
                .lootTableItem(TTBlocks.JAR_NODE.get()).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(TTDataComponents.NODE_DATA.get()))))));

        for (DeferredBlock<BlockCandleHolder> holder : TTBlocks.CANDLE_HOLDERS.values()) {
            add(holder.get(), candleHolderTable(holder.get()));
        }
        for (DyeColor dye : DyeColor.values()) {
            dropSelf(TTBlocks.CANDLES.get(dye).get());
            add(TTBlocks.BANNERS.get(dye).get(), bannerTable(TTItems.BANNERS.get(dye).get()));
            add(TTBlocks.WALL_BANNERS.get(dye).get(), bannerTable(TTItems.BANNERS.get(dye).get()));
        }
        add(TTBlocks.BANNER_CRIMSON_CULT.get(), bannerTable(TTItems.BANNER_CRIMSON_CULT.get()));
        add(TTBlocks.WALL_BANNER_CRIMSON_CULT.get(), bannerTable(TTItems.BANNER_CRIMSON_CULT.get()));
        generateResources();

        add(TTBlocks.LOOT_URN_COMMON.get(), lootContainerTable(TTBlocks.LOOT_URN_COMMON.get(), 0));
        add(TTBlocks.LOOT_URN_UNCOMMON.get(), lootContainerTable(TTBlocks.LOOT_URN_UNCOMMON.get(), 1));
        add(TTBlocks.LOOT_URN_RARE.get(), lootContainerTable(TTBlocks.LOOT_URN_RARE.get(), 2));
        add(TTBlocks.LOOT_CRATE_COMMON.get(), lootContainerTable(TTBlocks.LOOT_CRATE_COMMON.get(), 0));
        add(TTBlocks.LOOT_CRATE_UNCOMMON.get(), lootContainerTable(TTBlocks.LOOT_CRATE_UNCOMMON.get(), 1));
        add(TTBlocks.LOOT_CRATE_RARE.get(), lootContainerTable(TTBlocks.LOOT_CRATE_RARE.get(), 2));

        dropOther(TTBlocks.RESEARCH_TABLE.get(), TTBlocks.TABLE_WOOD.get());
        dropSelf(TTBlocks.DECONSTRUCTION_TABLE.get());
        dropSelf(TTBlocks.SPA.get());
        dropSelf(TTBlocks.FOCAL_MANIPULATOR.get());
        dropSelf(TTBlocks.ARCANE_WORKBENCH.get());
        dropSelf(TTBlocks.ARCANE_WORKBENCH_CHARGER.get());
        dropSelf(TTBlocks.CRUCIBLE.get());
        dropSelf(TTBlocks.ALEMBIC.get());
        dropSelf(TTBlocks.BELLOWS.get());
        dropSelf(TTBlocks.SMELTER_BASIC.get());
        dropSelf(TTBlocks.ESSENTIA_CRYSTALIZER.get());
        dropSelf(TTBlocks.ESSENTIA_RESERVOIR.get());
        dropSelf(TTBlocks.FLUX_SCRUBBER.get());
        dropSelf(TTBlocks.SMELTER_THAUMIUM.get());
        dropSelf(TTBlocks.SMELTER_VOID.get());
        dropSelf(TTBlocks.SMELTER_AUX.get());
        dropSelf(TTBlocks.SMELTER_VENT.get());
        add(TTBlocks.JAR_NORMAL.get(), jarLootTable(TTBlocks.JAR_NORMAL.get()));
        add(TTBlocks.JAR_VOID.get(), jarLootTable(TTBlocks.JAR_VOID.get()));
        dropSelf(TTBlocks.TUBE.get());
        dropSelf(TTBlocks.TUBE_VALVE.get());
        dropSelf(TTBlocks.TUBE_RESTRICT.get());
        dropSelf(TTBlocks.TUBE_FILTER.get());
        dropSelf(TTBlocks.TUBE_ONEWAY.get());
        dropSelf(TTBlocks.TUBE_BUFFER.get());

        for (DyeColor dye : DyeColor.values()) {
            dropSelf(TTBlocks.NITORS.get(dye).get());
        }
        add(TTBlocks.CRYSTAL_AER.get(), crystalTable(TTBlocks.CRYSTAL_AER.get()));
        add(TTBlocks.CRYSTAL_IGNIS.get(), crystalTable(TTBlocks.CRYSTAL_IGNIS.get()));
        add(TTBlocks.CRYSTAL_AQUA.get(), crystalTable(TTBlocks.CRYSTAL_AQUA.get()));
        add(TTBlocks.CRYSTAL_TERRA.get(), crystalTable(TTBlocks.CRYSTAL_TERRA.get()));
        add(TTBlocks.CRYSTAL_ORDO.get(), crystalTable(TTBlocks.CRYSTAL_ORDO.get()));
        add(TTBlocks.CRYSTAL_PERDITIO.get(), crystalTable(TTBlocks.CRYSTAL_PERDITIO.get()));
        add(TTBlocks.CRYSTAL_VITIUM.get(), crystalTable(TTBlocks.CRYSTAL_VITIUM.get()));
        dropSelf(TTBlocks.STONE_ARCANE.get());
        dropSelf(TTBlocks.STONE_ARCANE_BRICK.get());
        dropSelf(TTBlocks.STONE_ANCIENT.get());
        dropSelf(TTBlocks.STONE_ANCIENT_TILE.get());
        dropSelf(TTBlocks.STONE_ANCIENT_GLYPHED.get());
        dropSelf(TTBlocks.STONE_ELDRITCH_TILE.get());
        dropSelf(TTBlocks.STONE_POROUS.get());
        dropSelf(TTBlocks.STAIRS_ARCANE.get());
        dropSelf(TTBlocks.STAIRS_ARCANE_BRICK.get());
        dropSelf(TTBlocks.STAIRS_ANCIENT.get());
        add(TTBlocks.STONE_ANCIENT_ROCK.get(), noDrop());
        add(TTBlocks.STONE_ANCIENT_DOORWAY.get(), noDrop());
        dropSelf(TTBlocks.SAPLING_GREATWOOD.get());
        dropSelf(TTBlocks.SAPLING_SILVERWOOD.get());
        dropSelf(TTBlocks.LOG_GREATWOOD.get());
        dropSelf(TTBlocks.LOG_SILVERWOOD.get());
        add(TTBlocks.SILVERWOOD_NODE_LOG.get(), createSingleItemTable(TTItems.LOG_SILVERWOOD.get()));
        dropSelf(TTBlocks.WOOD_GREATWOOD.get());
        dropSelf(TTBlocks.WOOD_SILVERWOOD.get());
        dropSelf(TTBlocks.STRIPPED_LOG_GREATWOOD.get());
        dropSelf(TTBlocks.STRIPPED_LOG_SILVERWOOD.get());
        dropSelf(TTBlocks.STRIPPED_WOOD_GREATWOOD.get());
        dropSelf(TTBlocks.STRIPPED_WOOD_SILVERWOOD.get());
        dropSelf(TTBlocks.PLANK_GREATWOOD.get());
        dropSelf(TTBlocks.PLANK_SILVERWOOD.get());
        add(TTBlocks.LEAVES_GREATWOOD.get(), createLeavesDrops(TTBlocks.LEAVES_GREATWOOD.get(), TTBlocks.SAPLING_GREATWOOD.get()));
        add(TTBlocks.LEAVES_SILVERWOOD.get(), createLeavesDrops(TTBlocks.LEAVES_SILVERWOOD.get(), TTBlocks.SAPLING_SILVERWOOD.get()));
        dropSelf(TTBlocks.PLANT_SHIMMERLEAF.get());
        dropSelf(TTBlocks.ETHEREAL_BLOOM.get());
        dropSelf(TTBlocks.PLANT_CINDERPEARL.get());
        dropSelf(TTBlocks.PLANT_VISHROOM.get());
        add(TTBlocks.GRASS_AMBIENT.get(), block -> createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
        add(TTBlocks.TAINT_ROCK.get(), this::taintRockTable);
        dropSelf(TTBlocks.TAINT_SOIL.get());
        dropSelf(TTBlocks.TAINT_CRUST.get());
        dropSelf(TTBlocks.TAINT_GEYSER.get());
        dropSelf(TTBlocks.TAINT_LOG.get());
        dropSelf(TTBlocks.TAINT_FEATURE.get());
        add(TTBlocks.TAINT_FIBRE.get(), noDrop());
        add(TTBlocks.TAINT_SPORE_STALK.get(), noDrop());
    }

    private LootTable.Builder createLeavesDrops(Block leaves, Block sapling) {
        return createLeavesDrops(leaves, sapling, NORMAL_LEAVES_SAPLING_CHANCES);
    }

    private LootTable.Builder jarLootTable(Block block) {
        return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block).apply(CopyComponentsFunction
                .copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(TTDataComponents.ESSENTIA_CONTENTS.get()).include(TTDataComponents.ASPECT_FILTER.get())))));
    }

    private void generateResources() {
        add(TTBlocks.ORE_AMBER.get(), this::amberOreTable);
        add(TTBlocks.DEEPSLATE_ORE_AMBER.get(), this::amberOreTable);
        add(TTBlocks.MIRROR.get(), this::mirrorTable);
        add(TTBlocks.MIRROR_ESSENTIA.get(), this::mirrorTable);
        add(TTBlocks.MANA_POD.get(), this::manaPodTable);
        add(TTBlocks.ELDRITCH_CRAB_SPAWNER.get(),
                b -> LootTable.lootTable()
                        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(lookupProvider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), VENT_CURIO_CHANCES))
                                .add(LootItem.lootTableItem(TTItems.CURIO_PRESERVED.get()))));
        add(TTBlocks.ORE_CINNABAR.get(), b -> createOreDrop(b, TTItems.RAW_CINNABAR.get()));
        add(TTBlocks.DEEPSLATE_ORE_CINNABAR.get(), b -> createOreDrop(b, TTItems.RAW_CINNABAR.get()));
        add(TTBlocks.ORE_QUARTZ.get(), b -> createOreDrop(b, Items.QUARTZ));
        add(TTBlocks.DEEPSLATE_ORE_QUARTZ.get(), b -> createOreDrop(b, Items.QUARTZ));

        dropSelf(TTBlocks.ALCHEMICAL_CONSTRUCT.get());
        dropSelf(TTBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());
        dropSelf(TTBlocks.INFUSION_MATRIX.get());

        dropSelf(TTBlocks.METAL_BRASS_BLOCK.get());
        dropSelf(TTBlocks.METAL_THAUMIUM_BLOCK.get());
        dropSelf(TTBlocks.METAL_VOID_BLOCK.get());
        dropSelf(TTBlocks.AMBER_BLOCK.get());
        dropOther(TTBlocks.NETHER_BRICKS_PLACEHOLDER.get(), Blocks.NETHER_BRICKS);
        dropOther(TTBlocks.OBSIDIAN_PLACEHOLDER.get(), Blocks.OBSIDIAN);
        dropSelf(TTBlocks.LEVITATOR.get());
        dropSelf(TTBlocks.ITEM_GRATE.get());
        dropSelf(TTBlocks.TALLOW_BLOCK.get());
        dropSelf(TTBlocks.GOLEM_FETTER.get());
        dropSelf(TTBlocks.WARDED_GLASS.get());
        add(TTBlocks.ARCANE_DOOR.get(), this::createDoorTable);
        dropSelf(TTBlocks.ARCANE_PRESSURE_PLATE.get());
        dropSelf(TTBlocks.POTION_SPRAYER.get());
        dropSelf(TTBlocks.PATTERN_CRAFTER.get());
        dropSelf(TTBlocks.INLAY.get());
        dropSelf(TTBlocks.DIOPTRA.get());
        dropSelf(TTBlocks.ARCANE_EAR.get());
        dropSelf(TTBlocks.ARCANE_EAR_TOGGLE.get());
        dropSelf(TTBlocks.LAMP_ARCANE.get());
        dropSelf(TTBlocks.LAMP_GROWTH.get());
        dropSelf(TTBlocks.LAMP_FERTILITY.get());
        dropSelf(TTBlocks.CENTRIFUGE.get());
        dropSelf(TTBlocks.ARCANE_BORE.get());
        dropSelf(TTBlocks.HUNGRY_CHEST.get());
        dropSelf(TTBlocks.EVERFULL_URN.get());
        dropSelf(TTBlocks.VIS_GENERATOR.get());
        dropSelf(TTBlocks.ESSENTIA_INPUT.get());
        dropSelf(TTBlocks.ESSENTIA_OUTPUT.get());
        dropSelf(TTBlocks.CONDENSER.get());
        dropSelf(TTBlocks.CONDENSER_LATTICE.get());
        dropSelf(TTBlocks.CONDENSER_LATTICE_DIRTY.get());
        dropSelf(TTBlocks.STABILIZER.get());
        dropSelf(TTBlocks.REDSTONE_RELAY.get());
        dropSelf(TTBlocks.ACTIVATOR_RAIL.get());
        add(TTBlocks.SLAB_GREATWOOD.get(), this::createSlabItemTable);
        add(TTBlocks.SLAB_SILVERWOOD.get(), this::createSlabItemTable);
        add(TTBlocks.SLAB_ARCANE_STONE.get(), this::createSlabItemTable);
        add(TTBlocks.SLAB_ARCANE_BRICK.get(), this::createSlabItemTable);
        add(TTBlocks.SLAB_ANCIENT.get(), this::createSlabItemTable);
        add(TTBlocks.SLAB_ELDRITCH.get(), this::createSlabItemTable);
        dropSelf(TTBlocks.STAIRS_GREATWOOD.get());
        dropSelf(TTBlocks.STAIRS_SILVERWOOD.get());
        dropSelf(TTBlocks.ARCANE_GRINDSTONE.get());
        add(TTBlocks.DOOR_GREATWOOD.get(), this::createDoorTable);
        dropSelf(TTBlocks.TRAPDOOR_GREATWOOD.get());
        dropSelf(TTBlocks.FENCE_GREATWOOD.get());
        dropSelf(TTBlocks.FENCE_GATE_GREATWOOD.get());
        dropSelf(TTBlocks.BUTTON_GREATWOOD.get());
        dropSelf(TTBlocks.PRESSURE_PLATE_GREATWOOD.get());
        add(TTBlocks.DOOR_SILVERWOOD.get(), this::createDoorTable);
        dropSelf(TTBlocks.TRAPDOOR_SILVERWOOD.get());
        dropSelf(TTBlocks.FENCE_SILVERWOOD.get());
        dropSelf(TTBlocks.FENCE_GATE_SILVERWOOD.get());
        dropSelf(TTBlocks.BUTTON_SILVERWOOD.get());
        dropSelf(TTBlocks.PRESSURE_PLATE_SILVERWOOD.get());
        dropSelf(TTBlocks.TABLE_WOOD.get());
        dropSelf(TTBlocks.TABLE_STONE.get());
        dropSelf(TTBlocks.PAVING_STONE_TRAVEL.get());
        dropSelf(TTBlocks.PAVING_STONE_BARRIER.get());
        dropSelf(TTBlocks.AMBER_BRICK.get());
        dropSelf(TTBlocks.FLESH_BLOCK.get());
        dropSelf(TTBlocks.OBSIDIAN_TILE.get());
        dropSelf(TTBlocks.OBSIDIAN_TOTEM.get());
        dropOther(TTBlocks.OBSIDIAN_TOTEM_CHARGED.get(), TTBlocks.OBSIDIAN_TOTEM.get());
        dropSelf(TTBlocks.ELDRITCH_STONE.get());
        dropSelf(TTBlocks.ELDRITCH_STONE_INERT.get());
        dropSelf(TTBlocks.ELDRITCH_ROCK.get());
        dropSelf(TTBlocks.ELDRITCH_CRUST.get());
        dropSelf(TTBlocks.ELDRITCH_CRUST_GLOWING.get());
        dropSelf(TTBlocks.STAIRS_ELDRITCH.get());
        dropSelf(TTBlocks.ELDRITCH_PEDESTAL.get());
        add(TTBlocks.ELDRITCH_STONE_CRYSTAL.get(), createSingleItemTable(TTItems.CURIO_KNOWLEDGE.get()));
        dropSelf(TTBlocks.ELDRITCH_DOOR.get());
        dropSelf(TTBlocks.VOID_SIPHON.get());
        add(TTBlocks.THAUMATORIUM.get(), LootTable.lootTable().withPool(applyExplosionCondition(TTBlocks.THAUMATORIUM.get(), LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(TTBlocks.ALCHEMICAL_CONSTRUCT.get()).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2), false))))));
        dropSelf(TTBlocks.BRAIN_BOX.get());
        dropSelf(TTBlocks.VIS_BATTERY.get());
        dropSelf(TTBlocks.MATRIX_SPEED.get());
        dropSelf(TTBlocks.MATRIX_COST.get());
        add(TTBlocks.JAR_BRAIN.get(), bannerTable(TTBlocks.JAR_BRAIN.get()));
        dropOther(TTBlocks.GOLEM_BUILDER.get(), Blocks.PISTON);
        dropOther(TTBlocks.PLACEHOLDER_IRON_BARS.get(), Blocks.IRON_BARS);
        dropOther(TTBlocks.PLACEHOLDER_CAULDRON.get(), Blocks.CAULDRON);
        dropOther(TTBlocks.PLACEHOLDER_ANVIL.get(), Blocks.ANVIL);
        dropOther(TTBlocks.PLACEHOLDER_TABLE.get(), TTBlocks.TABLE_STONE.get());
        dropOther(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE.get(), TTBlocks.SMELTER_BASIC.get());
        dropOther(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER.get(), TTBlocks.ALEMBIC.get());
        dropOther(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER.get(), TTBlocks.ALCHEMICAL_CONSTRUCT.get());
        dropOther(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER.get(), TTBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());
        dropOther(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE.get(), TTBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());
        dropSelf(TTBlocks.PEDESTAL_ARCANE.get());
        dropSelf(TTBlocks.RECHARGE_PEDESTAL.get());
        dropSelf(TTBlocks.PEDESTAL_ANCIENT.get());
        dropSelf(TTBlocks.PEDESTAL_ELDRITCH.get());
        dropSelf(TTBlocks.PILLAR_ARCANE.get());
        dropSelf(TTBlocks.PILLAR_ANCIENT.get());
        dropSelf(TTBlocks.PILLAR_ELDRITCH.get());
    }

    private LootTable.Builder amberOreTable(Block block) {
        return createOreDrop(block, TTItems.AMBER.get()).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(this.doesNotHaveSilkTouch())
                .when(LootItemRandomChanceCondition.randomChance(AMBER_CURIO_CHANCE)).add(LootItem.lootTableItem(TTItems.CURIO_PRESERVED.get())));
    }
}
