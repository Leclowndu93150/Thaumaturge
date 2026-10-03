package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.decor.BlockCandleHolder;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class TCBlockTagsProvider extends BlockTagsProvider {
    public TCBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TCIds.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(BlockTags.DIRT).add(TCBlocks.GRASS_AMBIENT.get());
        tag(TCBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE).addTag(BlockTags.BASE_STONE_OVERWORLD).addTag(BlockTags.DIRT).addTag(BlockTags.LUSH_GROUND_REPLACEABLE);
        tag(BlockTags.FLOWER_POTS).add(TCBlocks.POTTED_SAPLING_GREATWOOD.get()).add(TCBlocks.POTTED_SAPLING_SILVERWOOD.get()).add(TCBlocks.POTTED_SHIMMERLEAF.get())
                .add(TCBlocks.POTTED_CINDERPEARL.get()).add(TCBlocks.POTTED_VISHROOM.get());

        tag(TCBlockTags.LAMP_GROWTH_BLACKLIST);
        tag(TCBlockTags.ARCANE_WORKBENCH_CHARGER_HOSTS).add(TCBlocks.ARCANE_WORKBENCH.get()).add(TCBlocks.FOCAL_MANIPULATOR.get());
        tag(TCBlockTags.PHYSICAL_FLUX).add(TCBlocks.FLUX_GOO.get()).add(TCBlocks.FLUX_GAS.get());
        tag(TCBlockTags.FLUX_SCRUBBABLE).addTag(TCBlockTags.PHYSICAL_FLUX);
        tag(TCBlockTags.CANDLES).addAll(TCBlocks.CANDLES.values().stream().map(DeferredHolder::get));
        tag(TCBlockTags.RESEARCH_BONUS_ORDO).addTag(TCBlockTags.CANDLES).addTag(BlockTags.BUTTONS).addTag(BlockTags.RAILS).addTag(BlockTags.FLOWER_POTS)
                .add(Blocks.TORCH, Blocks.WALL_TORCH, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH, Blocks.COPPER_TORCH, Blocks.COPPER_WALL_TORCH, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH)
                .add(Blocks.REDSTONE_WIRE, Blocks.LEVER, Blocks.REPEATER, Blocks.COMPARATOR, Blocks.LADDER, Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK)
                .add(Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD,
                        Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD, Blocks.PIGLIN_HEAD,
                        Blocks.PIGLIN_WALL_HEAD)
                .add(Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.PISTON_HEAD, Blocks.MOVING_PISTON);
        tag(TCBlockTags.TAINT_CONVERTIBLE_LOG).addTag(BlockTags.LOGS);
        tag(TCBlockTags.TAINT_CONVERTIBLE_SOIL).addTag(BlockTags.SAND).addTag(BlockTags.SUBSTRATE_OVERWORLD).add(Blocks.CLAY);
        tag(TCBlockTags.TAINT_CONVERTIBLE_ROCK).addTag(BlockTags.BASE_STONE_OVERWORLD).addTag(BlockTags.STONE_ORE_REPLACEABLES).addTag(BlockTags.STONE_BRICKS).addTag(Tags.Blocks.STONES)
                .addTag(Tags.Blocks.COBBLESTONES).addTag(Tags.Blocks.ORES);
        tag(TCBlockTags.TAINT_CONVERTIBLE_CRUST).add(Blocks.RED_MUSHROOM_BLOCK).add(Blocks.BROWN_MUSHROOM_BLOCK).add(Blocks.MUSHROOM_STEM).add(Blocks.PUMPKIN).add(Blocks.CARVED_PUMPKIN)
                .add(Blocks.JACK_O_LANTERN).add(Blocks.MELON).add(Blocks.CACTUS).add(Blocks.SPONGE).add(Blocks.WET_SPONGE).addTag(BlockTags.CORAL_BLOCKS).addTag(BlockTags.PLANKS);
        tag(TCBlockTags.TAINT_CONVERSION_IMMUNE);
        tag(TCBlockTags.WARDABLE_NON_SOLID).add(TCBlocks.WARDED_GLASS.get()).add(TCBlocks.ARCANE_DOOR.get()).add(TCBlocks.ARCANE_PRESSURE_PLATE.get());
        tag(TCBlockTags.ARCANE_LOCKS).add(TCBlocks.ARCANE_DOOR.get()).add(TCBlocks.ARCANE_PRESSURE_PLATE.get());
        tag(BlockTags.DOORS).add(TCBlocks.ARCANE_DOOR.get());
        tag(BlockTags.PRESSURE_PLATES).add(TCBlocks.ARCANE_PRESSURE_PLATE.get());
        tag(TCBlockTags.MAGICAL_PLANTS).add(TCBlocks.PLANT_SHIMMERLEAF.get()).add(TCBlocks.PLANT_CINDERPEARL.get()).add(TCBlocks.PLANT_VISHROOM.get());
        tag(BlockTags.FLOWERS).add(TCBlocks.PLANT_SHIMMERLEAF.get()).add(TCBlocks.PLANT_CINDERPEARL.get());
        tag(TCBlockTags.MAGICAL_FOREST_FLOWERS).add(Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP,
                Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY);
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TCBlocks.TUBE.get()).add(TCBlocks.TUBE_VALVE.get()).add(TCBlocks.TUBE_RESTRICT.get()).add(TCBlocks.TUBE_FILTER.get()).add(TCBlocks.TUBE_ONEWAY.get())
                .add(TCBlocks.TUBE_BUFFER.get()).add(TCBlocks.ESSENTIA_INPUT.get()).add(TCBlocks.ESSENTIA_OUTPUT.get()).add(TCBlocks.FOCAL_MANIPULATOR.get()).add(TCBlocks.ITEM_GRATE.get())
                .add(TCBlocks.TALLOW_BLOCK.get()).add(TCBlocks.GOLEM_FETTER.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(TCBlocks.CENTRIFUGE.get()).add(TCBlocks.ALEMBIC.get()).add(TCBlocks.ARCANE_WORKBENCH.get()).add(TCBlocks.RESEARCH_TABLE.get())
                .add(TCBlocks.ARCANE_WORKBENCH_CHARGER.get()).add(TCBlocks.ARCANE_DOOR.get()).add(TCBlocks.ARCANE_PRESSURE_PLATE.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TCBlocks.NODE_STABILIZER.get()).add(TCBlocks.NODE_STABILIZER_ADVANCED.get()).add(TCBlocks.NODE_TRANSDUCER.get()).add(TCBlocks.VIS_RELAY.get())
                .add(TCBlocks.CRUCIBLE.get()).add(TCBlocks.THAUMATORIUM.get()).add(TCBlocks.THAUMATORIUM_TOP.get()).add(TCBlocks.GOLEM_BUILDER.get()).add(TCBlocks.PLACEHOLDER_IRON_BARS.get())
                .add(TCBlocks.PLACEHOLDER_CAULDRON.get()).add(TCBlocks.PLACEHOLDER_ANVIL.get()).add(TCBlocks.PLACEHOLDER_TABLE.get()).add(TCBlocks.CONDENSER.get()).add(TCBlocks.PATTERN_CRAFTER.get())
                .add(TCBlocks.POTION_SPRAYER.get()).add(TCBlocks.DIOPTRA.get()).add(TCBlocks.LAMP_ARCANE.get()).add(TCBlocks.LAMP_GROWTH.get()).add(TCBlocks.LAMP_FERTILITY.get())
                .add(TCBlocks.EVERFULL_URN.get()).add(TCBlocks.VOID_SIPHON.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(TCBlocks.DECONSTRUCTION_TABLE.get()).add(TCBlocks.LEVITATOR.get()).add(TCBlocks.BRAIN_BOX.get()).add(TCBlocks.ARCANE_EAR.get())
                .add(TCBlocks.ARCANE_EAR_TOGGLE.get()).add(TCBlocks.HUNGRY_CHEST.get()).add(TCBlocks.VIS_GENERATOR.get());
        tag(TCBlockTags.INFUSION_STABILISERS).add(Blocks.SKELETON_SKULL).add(Blocks.SKELETON_WALL_SKULL).add(Blocks.WITHER_SKELETON_SKULL).add(Blocks.WITHER_SKELETON_WALL_SKULL)
                .add(Blocks.ZOMBIE_HEAD).add(Blocks.ZOMBIE_WALL_HEAD).add(Blocks.PLAYER_HEAD).add(Blocks.PLAYER_WALL_HEAD).add(Blocks.CREEPER_HEAD).add(Blocks.CREEPER_WALL_HEAD)
                .add(Blocks.DRAGON_HEAD).add(Blocks.DRAGON_WALL_HEAD).add(Blocks.PIGLIN_HEAD).add(Blocks.PIGLIN_WALL_HEAD);

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TCBlocks.SLAB_ARCANE_STONE.get()).add(TCBlocks.SLAB_ARCANE_BRICK.get()).add(TCBlocks.SLAB_ANCIENT.get()).add(TCBlocks.SLAB_ELDRITCH.get())
                .add(TCBlocks.TABLE_STONE.get()).add(TCBlocks.PAVING_STONE_TRAVEL.get()).add(TCBlocks.PAVING_STONE_BARRIER.get()).add(TCBlocks.AMBER_BRICK.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TCBlocks.STONE_ARCANE.get()).add(TCBlocks.STONE_ARCANE_BRICK.get()).add(TCBlocks.STONE_ANCIENT.get()).add(TCBlocks.STONE_ANCIENT_TILE.get())
                .add(TCBlocks.STONE_ANCIENT_GLYPHED.get()).add(TCBlocks.STONE_ELDRITCH_TILE.get()).add(TCBlocks.STONE_POROUS.get()).add(TCBlocks.STAIRS_ARCANE.get())
                .add(TCBlocks.STAIRS_ARCANE_BRICK.get()).add(TCBlocks.STAIRS_ANCIENT.get()).add(TCBlocks.PILLAR_ARCANE.get()).add(TCBlocks.PILLAR_ANCIENT.get()).add(TCBlocks.PILLAR_ELDRITCH.get())
                .add(TCBlocks.PEDESTAL_ARCANE.get()).add(TCBlocks.PEDESTAL_ANCIENT.get()).add(TCBlocks.PEDESTAL_ELDRITCH.get()).add(TCBlocks.RECHARGE_PEDESTAL.get()).add(TCBlocks.MATRIX_SPEED.get())
                .add(TCBlocks.MATRIX_COST.get()).add(TCBlocks.STABILIZER.get()).add(TCBlocks.INFUSION_MATRIX.get());

        tag(BlockTags.MINEABLE_WITH_AXE).add(TCBlocks.SLAB_GREATWOOD.get()).add(TCBlocks.SLAB_SILVERWOOD.get()).add(TCBlocks.STAIRS_GREATWOOD.get()).add(TCBlocks.STAIRS_SILVERWOOD.get())
                .add(TCBlocks.TABLE_WOOD.get());

        tag(BlockTags.SLABS).add(TCBlocks.SLAB_GREATWOOD.get()).add(TCBlocks.SLAB_SILVERWOOD.get()).add(TCBlocks.SLAB_ARCANE_STONE.get()).add(TCBlocks.SLAB_ARCANE_BRICK.get())
                .add(TCBlocks.SLAB_ANCIENT.get()).add(TCBlocks.SLAB_ELDRITCH.get());

        tag(BlockTags.WOODEN_SLABS).add(TCBlocks.SLAB_GREATWOOD.get()).add(TCBlocks.SLAB_SILVERWOOD.get());

        tag(BlockTags.STAIRS).add(TCBlocks.STAIRS_GREATWOOD.get()).add(TCBlocks.STAIRS_SILVERWOOD.get()).add(TCBlocks.STAIRS_ARCANE.get()).add(TCBlocks.STAIRS_ARCANE_BRICK.get())
                .add(TCBlocks.STAIRS_ANCIENT.get()).add(TCBlocks.STAIRS_ELDRITCH.get());

        tag(BlockTags.WOODEN_STAIRS).add(TCBlocks.STAIRS_GREATWOOD.get()).add(TCBlocks.STAIRS_SILVERWOOD.get());

        tag(TCBlockTags.ELDRITCH_OBELISK_PARTS).add(TCBlocks.ELDRITCH_ALTAR.get()).add(TCBlocks.ELDRITCH_OBELISK.get()).add(TCBlocks.ELDRITCH_PILLAR.get()).add(TCBlocks.ELDRITCH_CAPSTONE.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TCBlocks.OBSIDIAN_TILE.get()).add(TCBlocks.OBSIDIAN_TOTEM.get()).add(TCBlocks.OBSIDIAN_TOTEM_CHARGED.get()).add(TCBlocks.ELDRITCH_STONE.get())
                .add(TCBlocks.ELDRITCH_STONE_INERT.get()).add(TCBlocks.ELDRITCH_ROCK.get()).add(TCBlocks.ELDRITCH_CRUST.get()).add(TCBlocks.ELDRITCH_CRUST_GLOWING.get())
                .add(TCBlocks.STAIRS_ELDRITCH.get()).add(TCBlocks.ELDRITCH_PEDESTAL.get()).add(TCBlocks.ELDRITCH_STONE_CRYSTAL.get()).add(TCBlocks.ELDRITCH_CRAB_SPAWNER.get())
                .add(TCBlocks.ELDRITCH_TRAP.get());

        tag(BlockTags.RAILS).add(TCBlocks.ACTIVATOR_RAIL.get());

        tag(BlockTags.BEACON_BASE_BLOCKS).add(TCBlocks.METAL_THAUMIUM_BLOCK.get()).add(TCBlocks.METAL_BRASS_BLOCK.get()).add(TCBlocks.METAL_VOID_BLOCK.get());

        for (DeferredBlock<BlockCandleHolder> holder : TCBlocks.CANDLE_HOLDERS.values()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(holder.get());
        }
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TCBlocks.METAL_THAUMIUM_BLOCK.get()).add(TCBlocks.METAL_BRASS_BLOCK.get()).add(TCBlocks.METAL_VOID_BLOCK.get())
                .add(TCBlocks.OBSIDIAN_PLACEHOLDER.get()).add(TCBlocks.NETHER_BRICKS_PLACEHOLDER.get()).add(TCBlocks.INFERNAL_FURNACE.get()).add(TCBlocks.ARCANE_BORE.get());

        tag(BlockTags.NEEDS_IRON_TOOL).add(TCBlocks.METAL_THAUMIUM_BLOCK.get()).add(TCBlocks.METAL_BRASS_BLOCK.get()).add(TCBlocks.METAL_VOID_BLOCK.get());
        tag(BlockTags.NEEDS_DIAMOND_TOOL).add(TCBlocks.OBSIDIAN_TILE.get()).add(TCBlocks.OBSIDIAN_TOTEM.get()).add(TCBlocks.OBSIDIAN_TOTEM_CHARGED.get());

        tag(BlockTags.LOGS).add(TCBlocks.TAINT_LOG.get());

        tag(BlockTags.LOGS_THAT_BURN).addTag(TCBlockTags.GREATWOOD_LOGS).addTag(TCBlockTags.SILVERWOOD_LOGS);

        tag(BlockTags.LEAVES).add(TCBlocks.LEAVES_GREATWOOD.get()).add(TCBlocks.LEAVES_SILVERWOOD.get());

        tag(BlockTags.SAPLINGS).add(TCBlocks.SAPLING_GREATWOOD.get()).add(TCBlocks.SAPLING_SILVERWOOD.get());

        tag(BlockTags.PLANKS).addTags(TCBlockTags.PLANKS_GREATWOOD, TCBlockTags.PLANKS_SILVERWOOD);
        tag(TCBlockTags.PLANKS_GREATWOOD).add(TCBlocks.PLANK_GREATWOOD.get());
        tag(TCBlockTags.PLANKS_SILVERWOOD).add(TCBlocks.PLANK_SILVERWOOD.get());
        tag(TCBlockTags.PLANKS).addTags(TCBlockTags.PLANKS_GREATWOOD, TCBlockTags.PLANKS_SILVERWOOD);

        tag(BlockTags.MINEABLE_WITH_HOE).add(TCBlocks.LEAVES_GREATWOOD.get()).add(TCBlocks.LEAVES_SILVERWOOD.get());

        tag(TCBlockTags.CRUCIBLE_HEAT_SOURCES).add(Blocks.LAVA).add(Blocks.FIRE).add(Blocks.CAMPFIRE).add(Blocks.SOUL_FIRE).add(Blocks.SOUL_CAMPFIRE).add(Blocks.MAGMA_BLOCK)
                .addAll(TCBlocks.NITORS.values().stream().map(DeferredHolder::get));

        tag(TCBlockTags.SCAN_CLAY).add(Blocks.CLAY).addTag(BlockTags.TERRACOTTA);

        tag(TCBlockTags.ORES_AMBER).add(TCBlocks.ORE_AMBER.get());
        tag(TCBlockTags.ORES_CINNABAR).add(TCBlocks.ORE_CINNABAR.get());
        tag(Tags.Blocks.ORES_QUARTZ).add(TCBlocks.ORE_QUARTZ.get());
        tag(Tags.Blocks.ORES).addTags(TCBlockTags.ORES_AMBER, TCBlockTags.ORES_CINNABAR);

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TCBlocks.ORE_AMBER.get()).add(TCBlocks.ORE_CINNABAR.get()).add(TCBlocks.ORE_QUARTZ.get()).add(TCBlocks.SMELTER_BASIC.get())
                .add(TCBlocks.SMELTER_THAUMIUM.get()).add(TCBlocks.SMELTER_VOID.get()).add(TCBlocks.SMELTER_AUX.get()).add(TCBlocks.SMELTER_VENT.get()).add(TCBlocks.SPA.get())
                .add(TCBlocks.ALCHEMICAL_CONSTRUCT.get()).add(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get()).add(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE.get())
                .add(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER.get()).add(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER.get())
                .add(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER.get()).add(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE.get()).add(TCBlocks.ESSENTIA_CRYSTALIZER.get())
                .add(TCBlocks.ESSENTIA_RESERVOIR.get()).add(TCBlocks.FLUX_SCRUBBER.get());

        tag(BlockTags.NEEDS_STONE_TOOL).add(TCBlocks.ORE_AMBER.get());

        tag(BlockTags.NEEDS_IRON_TOOL).add(TCBlocks.ORE_CINNABAR.get());

        tag(TCBlockTags.PORTABLE_HOLE_BLACKLIST);

        tag(TCBlockTags.STORAGE_BLOCKS_AMBER).add(TCBlocks.AMBER_BLOCK.get());
        tag(TCBlockTags.STORAGE_BLOCKS_BRASS).add(TCBlocks.METAL_BRASS_BLOCK.get());
        tag(TCBlockTags.STORAGE_BLOCKS_THAUMIUM).add(TCBlocks.METAL_THAUMIUM_BLOCK.get());
        tag(TCBlockTags.STORAGE_BLOCKS_VOID_METAL).add(TCBlocks.METAL_VOID_BLOCK.get());
        tag(Tags.Blocks.STORAGE_BLOCKS).addTags(TCBlockTags.STORAGE_BLOCKS_AMBER, TCBlockTags.STORAGE_BLOCKS_BRASS, TCBlockTags.STORAGE_BLOCKS_THAUMIUM, TCBlockTags.STORAGE_BLOCKS_VOID_METAL);

        tag(TCBlockTags.GREATWOOD_LOGS).add(TCBlocks.LOG_GREATWOOD.get(), TCBlocks.WOOD_GREATWOOD.get(), TCBlocks.STRIPPED_LOG_GREATWOOD.get(), TCBlocks.STRIPPED_WOOD_GREATWOOD.get());
        tag(TCBlockTags.SILVERWOOD_LOGS).add(TCBlocks.LOG_SILVERWOOD.get(), TCBlocks.SILVERWOOD_NODE_LOG.get(), TCBlocks.WOOD_SILVERWOOD.get(), TCBlocks.STRIPPED_LOG_SILVERWOOD.get(),
                TCBlocks.STRIPPED_WOOD_SILVERWOOD.get());
        tag(Tags.Blocks.OVERWORLD_NATURAL_LOGS).add(TCBlocks.LOG_GREATWOOD.get(), TCBlocks.LOG_SILVERWOOD.get());
        tag(BlockTags.OVERWORLD_NATURAL_LOGS).add(TCBlocks.LOG_GREATWOOD.get(), TCBlocks.LOG_SILVERWOOD.get(), TCBlocks.SILVERWOOD_NODE_LOG.get());
        tag(BlockTags.SNAPS_GOAT_HORN).add(TCBlocks.LOG_GREATWOOD.get(), TCBlocks.LOG_SILVERWOOD.get(), TCBlocks.SILVERWOOD_NODE_LOG.get());
        tag(Tags.Blocks.NATURAL_WOODS).add(TCBlocks.WOOD_GREATWOOD.get()).add(TCBlocks.WOOD_SILVERWOOD.get());

        tag(Tags.Blocks.STRIPPED_LOGS).add(TCBlocks.STRIPPED_LOG_GREATWOOD.get()).add(TCBlocks.STRIPPED_LOG_SILVERWOOD.get());

        tag(Tags.Blocks.STRIPPED_WOODS).add(TCBlocks.STRIPPED_LOG_GREATWOOD.get()).add(TCBlocks.STRIPPED_LOG_SILVERWOOD.get());
    }
}
