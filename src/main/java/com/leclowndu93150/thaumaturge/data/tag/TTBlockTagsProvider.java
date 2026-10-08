package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.decor.BlockCandleHolder;
import com.leclowndu93150.thaumaturge.data.labyrinth.LabyrinthBlocks;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class TTBlockTagsProvider extends BlockTagsProvider {
    public TTBlockTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TTIds.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(TTBlockTags.CONNECTED_ELDRITCH_STONE)
                .add(TTBlocks.ELDRITCH_STONE.get())
                .add(TTBlocks.ELDRITCH_STONE_INERT.get())
                .add(TTBlocks.STAIRS_ELDRITCH.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TTBlocks.WALL_ARCANE_STONE.get())
                .add(TTBlocks.WALL_ARCANE_BRICK.get())
                .add(TTBlocks.STONE_ARCANE_PILLAR.get())
                .add(TTBlocks.WALL_ANCIENT.get())
                .add(TTBlocks.STAIRS_ANCIENT_TILE.get())
                .add(TTBlocks.WALL_ANCIENT_TILE.get())
                .add(TTBlocks.STAIRS_ANCIENT_ROCK.get())
                .add(TTBlocks.WALL_ANCIENT_ROCK.get())
                .add(TTBlocks.STONE_ANCIENT_PILLAR.get())
                .add(TTBlocks.WALL_ELDRITCH_STONE.get())
                .add(TTBlocks.STAIRS_ELDRITCH_TILE.get())
                .add(TTBlocks.WALL_ELDRITCH_TILE.get())
                .add(TTBlocks.STAIRS_ELDRITCH_ROCK.get())
                .add(TTBlocks.WALL_ELDRITCH_ROCK.get())
                .add(TTBlocks.STONE_ELDRITCH_PILLAR.get())
                .add(TTBlocks.SLAB_ANCIENT_TILE.get())
                .add(TTBlocks.SLAB_ANCIENT_ROCK.get())
                .add(TTBlocks.SLAB_ELDRITCH_STONE.get())
                .add(TTBlocks.SLAB_ELDRITCH_ROCK.get());

        tag(BlockTags.STAIRS)
                .add(TTBlocks.STAIRS_ANCIENT_TILE.get())
                .add(TTBlocks.STAIRS_ANCIENT_ROCK.get())
                .add(TTBlocks.STAIRS_ELDRITCH_TILE.get())
                .add(TTBlocks.STAIRS_ELDRITCH_ROCK.get());

        tag(BlockTags.SLABS)
                .add(TTBlocks.SLAB_ANCIENT_TILE.get())
                .add(TTBlocks.SLAB_ANCIENT_ROCK.get())
                .add(TTBlocks.SLAB_ELDRITCH_STONE.get())
                .add(TTBlocks.SLAB_ELDRITCH_ROCK.get());

        tag(BlockTags.WALLS)
                .add(TTBlocks.WALL_ARCANE_STONE.get())
                .add(TTBlocks.WALL_ARCANE_BRICK.get())
                .add(TTBlocks.WALL_ANCIENT.get())
                .add(TTBlocks.WALL_ANCIENT_TILE.get())
                .add(TTBlocks.WALL_ANCIENT_ROCK.get())
                .add(TTBlocks.WALL_ELDRITCH_STONE.get())
                .add(TTBlocks.WALL_ELDRITCH_TILE.get())
                .add(TTBlocks.WALL_ELDRITCH_ROCK.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(
                        TTBlocks.STONE_ELDRITCH_PILLAR.get(),
                        TTBlocks.STAIRS_ELDRITCH_TILE.get(),
                        TTBlocks.STAIRS_ELDRITCH_ROCK.get());
        tag(BlockTags.STAIRS).add(TTBlocks.STAIRS_ELDRITCH_TILE.get(), TTBlocks.STAIRS_ELDRITCH_ROCK.get());
        tag(TTBlockTags.CINDERPEARL_SOIL)
                .addTag(BlockTags.SAND)
                .addTag(BlockTags.DIRT)
                .addTag(BlockTags.TERRACOTTA);
        tag(TTBlockTags.SHIMMERLEAF_SOIL).addTag(BlockTags.DIRT);
        tag(TTBlockTags.VISHROOM_SOIL)
                .add(
                        Blocks.GRASS_BLOCK,
                        Blocks.DIRT,
                        Blocks.PODZOL,
                        Blocks.COARSE_DIRT,
                        Blocks.MYCELIUM,
                        Blocks.MOSS_BLOCK,
                        Blocks.STONE)
                .add(TTBlocks.GRASS_AMBIENT.get());

        tag(BlockTags.DIRT).add(TTBlocks.GRASS_AMBIENT.get());
        tag(TTBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE)
                .addTag(BlockTags.BASE_STONE_OVERWORLD)
                .addTag(BlockTags.DIRT)
                .addTag(BlockTags.LUSH_GROUND_REPLACEABLE);
        tag(TTBlockTags.LAMP_GROWTH_BLACKLIST);
        for (TagKey<Block> immune : List.of(BlockTags.WITHER_IMMUNE, BlockTags.DRAGON_IMMUNE)) {
            tag(immune).add(TTBlocks.STONE_ANCIENT_ROCK.get()).add(TTBlocks.STONE_ANCIENT_DOORWAY.get());
        }
        for (TagKey<Block> voidTag : List.of(
                BlockTags.WITHER_IMMUNE,
                BlockTags.DRAGON_IMMUNE,
                BlockTags.FEATURES_CANNOT_REPLACE,
                TTBlockTags.UNSAFE_LANDING)) {
            tag(voidTag).add(TTBlocks.ELDRITCH_NOTHING_DORMANT.get()).add(TTBlocks.ELDRITCH_NOTHING.get());
        }
        TagAppender<Block> candles = tag(TTBlockTags.CANDLES);
        TTBlocks.CANDLES.values().forEach(candle -> candles.add(candle.getKey()));
        tag(TTBlockTags.RESEARCH_BONUS_ORDO)
                .addTag(TTBlockTags.CANDLES)
                .addTag(BlockTags.BUTTONS)
                .addTag(BlockTags.RAILS)
                .addTag(BlockTags.FLOWER_POTS)
                .add(
                        Blocks.TORCH,
                        Blocks.WALL_TORCH,
                        Blocks.SOUL_TORCH,
                        Blocks.SOUL_WALL_TORCH,
                        Blocks.REDSTONE_TORCH,
                        Blocks.REDSTONE_WALL_TORCH)
                .add(
                        Blocks.REDSTONE_WIRE,
                        Blocks.LEVER,
                        Blocks.REPEATER,
                        Blocks.COMPARATOR,
                        Blocks.LADDER,
                        Blocks.TRIPWIRE,
                        Blocks.TRIPWIRE_HOOK)
                .add(
                        Blocks.SKELETON_SKULL,
                        Blocks.SKELETON_WALL_SKULL,
                        Blocks.WITHER_SKELETON_SKULL,
                        Blocks.WITHER_SKELETON_WALL_SKULL,
                        Blocks.ZOMBIE_HEAD,
                        Blocks.ZOMBIE_WALL_HEAD,
                        Blocks.PLAYER_HEAD,
                        Blocks.PLAYER_WALL_HEAD,
                        Blocks.CREEPER_HEAD,
                        Blocks.CREEPER_WALL_HEAD,
                        Blocks.DRAGON_HEAD,
                        Blocks.DRAGON_WALL_HEAD,
                        Blocks.PIGLIN_HEAD,
                        Blocks.PIGLIN_WALL_HEAD)
                .add(Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.PISTON_HEAD, Blocks.MOVING_PISTON);
        tag(TTBlockTags.ARCANE_WORKBENCH_CHARGER_HOSTS)
                .add(TTBlocks.ARCANE_WORKBENCH.get())
                .add(TTBlocks.FOCAL_MANIPULATOR.get());
        tag(TTBlockTags.INFUSION_STABILISERS)
                .add(Blocks.SKELETON_SKULL)
                .add(Blocks.SKELETON_WALL_SKULL)
                .add(Blocks.WITHER_SKELETON_SKULL)
                .add(Blocks.WITHER_SKELETON_WALL_SKULL)
                .add(Blocks.ZOMBIE_HEAD)
                .add(Blocks.ZOMBIE_WALL_HEAD)
                .add(Blocks.PLAYER_HEAD)
                .add(Blocks.PLAYER_WALL_HEAD)
                .add(Blocks.CREEPER_HEAD)
                .add(Blocks.CREEPER_WALL_HEAD)
                .add(Blocks.DRAGON_HEAD)
                .add(Blocks.DRAGON_WALL_HEAD)
                .add(Blocks.PIGLIN_HEAD)
                .add(Blocks.PIGLIN_WALL_HEAD);

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TTBlocks.TUBE.get())
                .add(TTBlocks.TUBE_VALVE.get())
                .add(TTBlocks.TUBE_RESTRICT.get())
                .add(TTBlocks.TUBE_FILTER.get())
                .add(TTBlocks.TUBE_ONEWAY.get())
                .add(TTBlocks.TUBE_BUFFER.get())
                .add(TTBlocks.ESSENTIA_INPUT.get())
                .add(TTBlocks.ESSENTIA_OUTPUT.get())
                .add(TTBlocks.FOCAL_MANIPULATOR.get())
                .add(TTBlocks.INFUSION_MATRIX.get())
                .add(TTBlocks.FLUX_SCRUBBER.get())
                .add(TTBlocks.SLAB_ARCANE_STONE.get())
                .add(TTBlocks.SLAB_ARCANE_BRICK.get())
                .add(TTBlocks.SLAB_ANCIENT.get())
                .add(TTBlocks.SLAB_ELDRITCH.get())
                .add(TTBlocks.TABLE_STONE.get())
                .add(TTBlocks.PAVING_STONE_TRAVEL.get())
                .add(TTBlocks.PAVING_STONE_BARRIER.get())
                .add(TTBlocks.AMBER_BRICK.get());

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(TTBlocks.CENTRIFUGE.get())
                .add(TTBlocks.ALEMBIC.get())
                .add(TTBlocks.ARCANE_WORKBENCH.get())
                .add(TTBlocks.RESEARCH_TABLE.get())
                .add(TTBlocks.ARCANE_WORKBENCH_CHARGER.get())
                .add(TTBlocks.SLAB_GREATWOOD.get())
                .add(TTBlocks.SLAB_SILVERWOOD.get())
                .add(TTBlocks.STAIRS_GREATWOOD.get())
                .add(TTBlocks.STAIRS_SILVERWOOD.get())
                .add(TTBlocks.TABLE_WOOD.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TTBlocks.NODE_STABILIZER.get())
                .add(TTBlocks.NODE_STABILIZER_ADVANCED.get())
                .add(TTBlocks.NODE_TRANSDUCER.get())
                .add(TTBlocks.VIS_RELAY.get())
                .add(TTBlocks.CRUCIBLE.get())
                .add(TTBlocks.THAUMATORIUM.get())
                .add(TTBlocks.THAUMATORIUM_TOP.get())
                .add(TTBlocks.GOLEM_BUILDER.get())
                .add(TTBlocks.PLACEHOLDER_IRON_BARS.get())
                .add(TTBlocks.PLACEHOLDER_CAULDRON.get())
                .add(TTBlocks.PLACEHOLDER_ANVIL.get())
                .add(TTBlocks.PLACEHOLDER_TABLE.get())
                .add(TTBlocks.CONDENSER.get())
                .add(TTBlocks.PATTERN_CRAFTER.get())
                .add(TTBlocks.POTION_SPRAYER.get())
                .add(TTBlocks.DIOPTRA.get())
                .add(TTBlocks.LAMP_ARCANE.get())
                .add(TTBlocks.LAMP_GROWTH.get())
                .add(TTBlocks.LAMP_FERTILITY.get())
                .add(TTBlocks.EVERFULL_URN.get())
                .add(TTBlocks.VOID_SIPHON.get());
        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(TTBlocks.DECONSTRUCTION_TABLE.get())
                .add(TTBlocks.LEVITATOR.get())
                .add(TTBlocks.BRAIN_BOX.get())
                .add(TTBlocks.ARCANE_EAR.get())
                .add(TTBlocks.ARCANE_EAR_TOGGLE.get())
                .add(TTBlocks.HUNGRY_CHEST.get())
                .add(TTBlocks.VIS_GENERATOR.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TTBlocks.ARCANE_GRINDSTONE.get())
                .add(TTBlocks.ESSENTIA_CRYSTALIZER.get())
                .add(TTBlocks.ESSENTIA_RESERVOIR.get())
                .add(TTBlocks.GOLEM_FETTER.get())
                .add(TTBlocks.ITEM_GRATE.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(TTBlocks.ARCANE_DOOR.get()).add(TTBlocks.ARCANE_PRESSURE_PLATE.get());

        tag(BlockTags.SLABS)
                .add(TTBlocks.SLAB_GREATWOOD.get())
                .add(TTBlocks.SLAB_SILVERWOOD.get())
                .add(TTBlocks.SLAB_ARCANE_STONE.get())
                .add(TTBlocks.SLAB_ARCANE_BRICK.get())
                .add(TTBlocks.SLAB_ANCIENT.get())
                .add(TTBlocks.SLAB_ELDRITCH.get());

        tag(BlockTags.WOODEN_SLABS).add(TTBlocks.SLAB_GREATWOOD.get()).add(TTBlocks.SLAB_SILVERWOOD.get());

        tag(BlockTags.STAIRS)
                .add(TTBlocks.STAIRS_GREATWOOD.get())
                .add(TTBlocks.STAIRS_SILVERWOOD.get())
                .add(TTBlocks.STAIRS_ARCANE.get())
                .add(TTBlocks.STAIRS_ARCANE_BRICK.get())
                .add(TTBlocks.STAIRS_ANCIENT.get())
                .add(TTBlocks.STAIRS_ELDRITCH.get());

        tag(BlockTags.WOODEN_STAIRS).add(TTBlocks.STAIRS_GREATWOOD.get()).add(TTBlocks.STAIRS_SILVERWOOD.get());

        tag(BlockTags.WOODEN_DOORS).add(TTBlocks.DOOR_GREATWOOD.get()).add(TTBlocks.DOOR_SILVERWOOD.get());
        tag(BlockTags.WOODEN_TRAPDOORS).add(TTBlocks.TRAPDOOR_GREATWOOD.get()).add(TTBlocks.TRAPDOOR_SILVERWOOD.get());
        tag(BlockTags.WOODEN_FENCES).add(TTBlocks.FENCE_GREATWOOD.get()).add(TTBlocks.FENCE_SILVERWOOD.get());
        tag(BlockTags.FENCE_GATES).add(TTBlocks.FENCE_GATE_GREATWOOD.get()).add(TTBlocks.FENCE_GATE_SILVERWOOD.get());
        tag(BlockTags.WOODEN_BUTTONS).add(TTBlocks.BUTTON_GREATWOOD.get()).add(TTBlocks.BUTTON_SILVERWOOD.get());
        tag(BlockTags.WOODEN_PRESSURE_PLATES)
                .add(TTBlocks.PRESSURE_PLATE_GREATWOOD.get())
                .add(TTBlocks.PRESSURE_PLATE_SILVERWOOD.get());
        tag(Tags.Blocks.FENCES_WOODEN).add(TTBlocks.FENCE_GREATWOOD.get()).add(TTBlocks.FENCE_SILVERWOOD.get());
        tag(Tags.Blocks.FENCE_GATES_WOODEN)
                .add(TTBlocks.FENCE_GATE_GREATWOOD.get())
                .add(TTBlocks.FENCE_GATE_SILVERWOOD.get());
        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(TTBlocks.DOOR_GREATWOOD.get())
                .add(TTBlocks.TRAPDOOR_GREATWOOD.get())
                .add(TTBlocks.FENCE_GREATWOOD.get())
                .add(TTBlocks.FENCE_GATE_GREATWOOD.get())
                .add(TTBlocks.BUTTON_GREATWOOD.get())
                .add(TTBlocks.PRESSURE_PLATE_GREATWOOD.get())
                .add(TTBlocks.DOOR_SILVERWOOD.get())
                .add(TTBlocks.TRAPDOOR_SILVERWOOD.get())
                .add(TTBlocks.FENCE_SILVERWOOD.get())
                .add(TTBlocks.FENCE_GATE_SILVERWOOD.get())
                .add(TTBlocks.BUTTON_SILVERWOOD.get())
                .add(TTBlocks.PRESSURE_PLATE_SILVERWOOD.get());

        tag(TTBlockTags.ELDRITCH_OBELISK_PARTS)
                .add(TTBlocks.ELDRITCH_ALTAR.get())
                .add(TTBlocks.ELDRITCH_OBELISK.get())
                .add(TTBlocks.ELDRITCH_PILLAR.get())
                .add(TTBlocks.ELDRITCH_CAPSTONE.get());

        tag(TTBlockTags.LABYRINTH_BARRIER).add(TTBlocks.ELDRITCH_DOOR.get());
        for (Block passable : LabyrinthBlocks.passableBlocks()) {
            tag(TTBlockTags.LABYRINTH_PASSABLE).add(passable);
        }
        tag(TTBlockTags.UNSAFE_LANDING)
                .add(TTBlocks.ELDRITCH_PORTAL.get())
                .add(TTBlocks.ELDRITCH_TRAP.get())
                .add(Blocks.MAGMA_BLOCK)
                .add(Blocks.CACTUS)
                .add(Blocks.SWEET_BERRY_BUSH)
                .add(Blocks.POWDER_SNOW)
                .addTag(BlockTags.FIRE)
                .addTag(BlockTags.CAMPFIRES);

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TTBlocks.OBSIDIAN_TILE.get())
                .add(TTBlocks.OBSIDIAN_TOTEM.get())
                .add(TTBlocks.OBSIDIAN_TOTEM_CHARGED.get())
                .add(TTBlocks.ELDRITCH_STONE.get())
                .add(TTBlocks.ELDRITCH_STONE_INERT.get())
                .add(TTBlocks.ELDRITCH_ROCK.get())
                .add(TTBlocks.ELDRITCH_CRUST.get())
                .add(TTBlocks.ELDRITCH_CRUST_GLOWING.get())
                .add(TTBlocks.STAIRS_ELDRITCH.get())
                .add(TTBlocks.ELDRITCH_PEDESTAL.get())
                .add(TTBlocks.ELDRITCH_STONE_CRYSTAL.get())
                .add(TTBlocks.ELDRITCH_CRAB_SPAWNER.get())
                .add(TTBlocks.ELDRITCH_TRAP.get());

        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(TTBlocks.OBSIDIAN_TILE.get())
                .add(TTBlocks.OBSIDIAN_TOTEM.get())
                .add(TTBlocks.OBSIDIAN_TOTEM_CHARGED.get());

        tag(BlockTags.RAILS).add(TTBlocks.ACTIVATOR_RAIL.get());

        tag(BlockTags.BEACON_BASE_BLOCKS)
                .add(TTBlocks.METAL_THAUMIUM_BLOCK.get())
                .add(TTBlocks.METAL_BRASS_BLOCK.get())
                .add(TTBlocks.METAL_VOID_BLOCK.get());

        for (DeferredBlock<BlockCandleHolder> holder : TTBlocks.CANDLE_HOLDERS.values()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(holder.get());
        }

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TTBlocks.METAL_THAUMIUM_BLOCK.get())
                .add(TTBlocks.METAL_BRASS_BLOCK.get())
                .add(TTBlocks.METAL_VOID_BLOCK.get())
                .add(TTBlocks.OBSIDIAN_PLACEHOLDER.get())
                .add(TTBlocks.NETHER_BRICKS_PLACEHOLDER.get())
                .add(TTBlocks.INFERNAL_FURNACE.get())
                .add(TTBlocks.ARCANE_BORE.get());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(TTBlocks.METAL_THAUMIUM_BLOCK.get())
                .add(TTBlocks.METAL_BRASS_BLOCK.get())
                .add(TTBlocks.METAL_VOID_BLOCK.get());

        tag(BlockTags.LOGS_THAT_BURN).addTag(TTBlockTags.GREATWOOD_LOGS).addTag(TTBlockTags.SILVERWOOD_LOGS);
        tag(BlockTags.LOGS)
                .addTag(TTBlockTags.GREATWOOD_LOGS)
                .addTag(TTBlockTags.SILVERWOOD_LOGS)
                .add(TTBlocks.TAINT_LOG.get());

        tag(BlockTags.LEAVES).add(TTBlocks.LEAVES_GREATWOOD.get()).add(TTBlocks.LEAVES_SILVERWOOD.get());

        tag(BlockTags.SAPLINGS).add(TTBlocks.SAPLING_GREATWOOD.get()).add(TTBlocks.SAPLING_SILVERWOOD.get());

        tag(TTBlockTags.PLANKS_GREATWOOD).add(TTBlocks.PLANK_GREATWOOD.get());
        tag(TTBlockTags.PLANKS_SILVERWOOD).add(TTBlocks.PLANK_SILVERWOOD.get());
        tag(TTBlockTags.PLANKS).addTags(TTBlockTags.PLANKS_GREATWOOD, TTBlockTags.PLANKS_SILVERWOOD);
        tag(BlockTags.PLANKS).addTags(TTBlockTags.PLANKS_GREATWOOD, TTBlockTags.PLANKS_SILVERWOOD);

        tag(BlockTags.MINEABLE_WITH_HOE).add(TTBlocks.LEAVES_GREATWOOD.get()).add(TTBlocks.LEAVES_SILVERWOOD.get());

        tag(TTBlockTags.PHYSICAL_FLUX).add(TTBlocks.FLUX_GOO.get(), TTBlocks.FLUX_GAS.get());
        tag(TTBlockTags.FLUX_SCRUBBABLE).addTag(TTBlockTags.PHYSICAL_FLUX);
        tag(TTBlockTags.TAINT_CONVERTIBLE_LOG).addTag(BlockTags.LOGS);
        tag(TTBlockTags.TAINT_CONVERTIBLE_SOIL)
                .addTag(BlockTags.SAND)
                .addTag(BlockTags.DIRT)
                .add(Blocks.CLAY);
        tag(TTBlockTags.TAINT_CONVERTIBLE_ROCK)
                .addTag(BlockTags.BASE_STONE_OVERWORLD)
                .addTag(BlockTags.STONE_ORE_REPLACEABLES)
                .addTag(BlockTags.STONE_BRICKS)
                .addTag(Tags.Blocks.STONES)
                .addTag(Tags.Blocks.COBBLESTONES)
                .addTag(Tags.Blocks.ORES);
        tag(TTBlockTags.TAINT_CONVERTIBLE_CRUST)
                .add(Blocks.RED_MUSHROOM_BLOCK)
                .add(Blocks.BROWN_MUSHROOM_BLOCK)
                .add(Blocks.MUSHROOM_STEM)
                .add(Blocks.PUMPKIN)
                .add(Blocks.CARVED_PUMPKIN)
                .add(Blocks.JACK_O_LANTERN)
                .add(Blocks.MELON)
                .add(Blocks.CACTUS)
                .add(Blocks.SPONGE)
                .add(Blocks.WET_SPONGE)
                .addTag(BlockTags.CORAL_BLOCKS)
                .addTag(BlockTags.PLANKS);
        tag(TTBlockTags.TAINT_CONVERSION_IMMUNE);

        tag(TTBlockTags.CRUCIBLE_HEAT_SOURCES)
                .add(Blocks.LAVA)
                .add(Blocks.FIRE)
                .add(Blocks.CAMPFIRE)
                .add(Blocks.SOUL_FIRE)
                .add(Blocks.SOUL_CAMPFIRE)
                .add(Blocks.MAGMA_BLOCK)
                .add(TTBlocks.NITORS.values().stream().map(DeferredHolder::get).toArray(Block[]::new));

        tag(TTBlockTags.SCAN_CLAY).add(Blocks.CLAY).addTag(BlockTags.TERRACOTTA);

        tag(TTBlockTags.ORES_AMBER).add(TTBlocks.ORE_AMBER.get()).add(TTBlocks.DEEPSLATE_ORE_AMBER.get());
        tag(TTBlockTags.ORES_CINNABAR).add(TTBlocks.ORE_CINNABAR.get()).add(TTBlocks.DEEPSLATE_ORE_CINNABAR.get());
        tag(Tags.Blocks.ORES_QUARTZ).add(TTBlocks.ORE_QUARTZ.get()).add(TTBlocks.DEEPSLATE_ORE_QUARTZ.get());
        tag(Tags.Blocks.ORES_IN_GROUND_STONE)
                .add(TTBlocks.ORE_AMBER.get())
                .add(TTBlocks.ORE_CINNABAR.get())
                .add(TTBlocks.ORE_QUARTZ.get());
        tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE)
                .add(TTBlocks.DEEPSLATE_ORE_AMBER.get())
                .add(TTBlocks.DEEPSLATE_ORE_CINNABAR.get())
                .add(TTBlocks.DEEPSLATE_ORE_QUARTZ.get());
        tag(Tags.Blocks.ORES).addTags(TTBlockTags.ORES_AMBER, TTBlockTags.ORES_CINNABAR, TTBlockTags.ORES_QUARTZ);

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TTBlocks.ORE_AMBER.get())
                .add(TTBlocks.ORE_CINNABAR.get())
                .add(TTBlocks.ORE_QUARTZ.get())
                .add(TTBlocks.DEEPSLATE_ORE_AMBER.get())
                .add(TTBlocks.DEEPSLATE_ORE_CINNABAR.get())
                .add(TTBlocks.DEEPSLATE_ORE_QUARTZ.get())
                .add(TTBlocks.SMELTER_BASIC.get())
                .add(TTBlocks.SMELTER_THAUMIUM.get())
                .add(TTBlocks.SMELTER_VOID.get())
                .add(TTBlocks.SMELTER_AUX.get())
                .add(TTBlocks.SMELTER_VENT.get())
                .add(TTBlocks.SPA.get())
                .add(TTBlocks.PEDESTAL_ARCANE.get())
                .add(TTBlocks.PEDESTAL_ANCIENT.get())
                .add(TTBlocks.PEDESTAL_ELDRITCH.get())
                .add(TTBlocks.ALCHEMICAL_CONSTRUCT.get())
                .add(TTBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TTBlocks.MATRIX_SPEED.get())
                .add(TTBlocks.MATRIX_COST.get())
                .add(TTBlocks.STABILIZER.get())
                .add(TTBlocks.STONE_ARCANE.get())
                .add(TTBlocks.STONE_ARCANE_BRICK.get())
                .add(TTBlocks.STONE_ANCIENT.get())
                .add(TTBlocks.STONE_ANCIENT_TILE.get())
                .add(TTBlocks.STONE_ANCIENT_GLYPHED.get())
                .add(TTBlocks.STONE_ELDRITCH_TILE.get())
                .add(TTBlocks.STONE_POROUS.get())
                .add(TTBlocks.STAIRS_ARCANE.get())
                .add(TTBlocks.STAIRS_ARCANE_BRICK.get())
                .add(TTBlocks.STAIRS_ANCIENT.get())
                .add(TTBlocks.PILLAR_ARCANE.get())
                .add(TTBlocks.PILLAR_ANCIENT.get())
                .add(TTBlocks.PILLAR_ELDRITCH.get())
                .add(TTBlocks.RECHARGE_PEDESTAL.get());

        tag(BlockTags.NEEDS_STONE_TOOL).add(TTBlocks.ORE_AMBER.get()).add(TTBlocks.DEEPSLATE_ORE_AMBER.get());

        tag(BlockTags.NEEDS_IRON_TOOL).add(TTBlocks.ORE_CINNABAR.get()).add(TTBlocks.DEEPSLATE_ORE_CINNABAR.get());

        tag(TTBlockTags.PORTABLE_HOLE_BLACKLIST);

        tag(TTBlockTags.STORAGE_BLOCKS_AMBER).add(TTBlocks.AMBER_BLOCK.get());
        tag(TTBlockTags.STORAGE_BLOCKS_BRASS).add(TTBlocks.METAL_BRASS_BLOCK.get());
        tag(TTBlockTags.STORAGE_BLOCKS_THAUMIUM).add(TTBlocks.METAL_THAUMIUM_BLOCK.get());
        tag(TTBlockTags.STORAGE_BLOCKS_VOID_METAL).add(TTBlocks.METAL_VOID_BLOCK.get());
        tag(Tags.Blocks.STORAGE_BLOCKS)
                .addTags(
                        TTBlockTags.STORAGE_BLOCKS_AMBER,
                        TTBlockTags.STORAGE_BLOCKS_BRASS,
                        TTBlockTags.STORAGE_BLOCKS_THAUMIUM,
                        TTBlockTags.STORAGE_BLOCKS_VOID_METAL);

        tag(TTBlockTags.GREATWOOD_LOGS)
                .add(TTBlocks.LOG_GREATWOOD.get())
                .add(TTBlocks.WOOD_GREATWOOD.get())
                .add(TTBlocks.STRIPPED_LOG_GREATWOOD.get())
                .add(TTBlocks.STRIPPED_WOOD_GREATWOOD.get());

        tag(TTBlockTags.SILVERWOOD_LOGS)
                .add(TTBlocks.LOG_SILVERWOOD.get())
                .add(TTBlocks.SILVERWOOD_NODE_LOG.get())
                .add(TTBlocks.WOOD_SILVERWOOD.get())
                .add(TTBlocks.STRIPPED_LOG_SILVERWOOD.get())
                .add(TTBlocks.STRIPPED_WOOD_SILVERWOOD.get());

        tag(BlockTags.OVERWORLD_NATURAL_LOGS)
                .add(TTBlocks.LOG_GREATWOOD.get())
                .add(TTBlocks.LOG_SILVERWOOD.get())
                .add(TTBlocks.SILVERWOOD_NODE_LOG.get());

        tag(BlockTags.SNAPS_GOAT_HORN)
                .add(TTBlocks.LOG_GREATWOOD.get())
                .add(TTBlocks.LOG_SILVERWOOD.get())
                .add(TTBlocks.SILVERWOOD_NODE_LOG.get());

        tag(BlockTags.FLOWERS).add(TTBlocks.PLANT_SHIMMERLEAF.get()).add(TTBlocks.PLANT_CINDERPEARL.get());

        tag(TTBlockTags.MAGICAL_PLANTS)
                .add(TTBlocks.PLANT_SHIMMERLEAF.get())
                .add(TTBlocks.PLANT_CINDERPEARL.get())
                .add(TTBlocks.PLANT_VISHROOM.get());

        tag(TTBlockTags.MAGICAL_FOREST_FLOWERS)
                .add(Blocks.DANDELION)
                .add(Blocks.POPPY)
                .add(Blocks.BLUE_ORCHID)
                .add(Blocks.ALLIUM)
                .add(Blocks.AZURE_BLUET)
                .add(Blocks.RED_TULIP)
                .add(Blocks.ORANGE_TULIP)
                .add(Blocks.WHITE_TULIP)
                .add(Blocks.PINK_TULIP)
                .add(Blocks.OXEYE_DAISY)
                .add(Blocks.CORNFLOWER)
                .add(Blocks.LILY_OF_THE_VALLEY);

        tag(Tags.Blocks.STRIPPED_LOGS)
                .add(TTBlocks.STRIPPED_LOG_GREATWOOD.get())
                .add(TTBlocks.STRIPPED_LOG_SILVERWOOD.get());

        tag(Tags.Blocks.STRIPPED_WOODS)
                .add(TTBlocks.STRIPPED_WOOD_GREATWOOD.get())
                .add(TTBlocks.STRIPPED_WOOD_SILVERWOOD.get());
    }
}
