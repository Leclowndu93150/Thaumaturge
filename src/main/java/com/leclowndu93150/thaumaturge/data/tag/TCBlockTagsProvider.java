package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class TCBlockTagsProvider extends BlockTagsProvider {
    public TCBlockTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TCIds.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(TCBlockTags.LAMP_GROWTH_BLACKLIST);
        tag(TCBlockTags.INFUSION_STABILISERS)
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
                .add(TCBlocks.SLAB_ARCANE_STONE.get())
                .add(TCBlocks.SLAB_ARCANE_BRICK.get())
                .add(TCBlocks.SLAB_ANCIENT.get())
                .add(TCBlocks.SLAB_ELDRITCH.get())
                .add(TCBlocks.TABLE_STONE.get())
                .add(TCBlocks.PAVING_STONE_TRAVEL.get())
                .add(TCBlocks.PAVING_STONE_BARRIER.get())
                .add(TCBlocks.AMBER_BRICK.get());

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(TCBlocks.SLAB_GREATWOOD.get())
                .add(TCBlocks.SLAB_SILVERWOOD.get())
                .add(TCBlocks.STAIRS_GREATWOOD.get())
                .add(TCBlocks.STAIRS_SILVERWOOD.get())
                .add(TCBlocks.TABLE_WOOD.get());

        tag(BlockTags.SLABS)
                .add(TCBlocks.SLAB_GREATWOOD.get())
                .add(TCBlocks.SLAB_SILVERWOOD.get())
                .add(TCBlocks.SLAB_ARCANE_STONE.get())
                .add(TCBlocks.SLAB_ARCANE_BRICK.get())
                .add(TCBlocks.SLAB_ANCIENT.get())
                .add(TCBlocks.SLAB_ELDRITCH.get());

        tag(BlockTags.WOODEN_SLABS).add(TCBlocks.SLAB_GREATWOOD.get()).add(TCBlocks.SLAB_SILVERWOOD.get());

        tag(BlockTags.STAIRS)
                .add(TCBlocks.STAIRS_GREATWOOD.get())
                .add(TCBlocks.STAIRS_SILVERWOOD.get())
                .add(TCBlocks.STAIRS_ARCANE.get())
                .add(TCBlocks.STAIRS_ARCANE_BRICK.get())
                .add(TCBlocks.STAIRS_ANCIENT.get())
                .add(TCBlocks.STAIRS_ELDRITCH.get());

        tag(BlockTags.WOODEN_STAIRS).add(TCBlocks.STAIRS_GREATWOOD.get()).add(TCBlocks.STAIRS_SILVERWOOD.get());

        tag(TCBlockTags.ELDRITCH_OBELISK_PARTS)
                .add(TCBlocks.ELDRITCH_ALTAR.get())
                .add(TCBlocks.ELDRITCH_OBELISK.get())
                .add(TCBlocks.ELDRITCH_PILLAR.get())
                .add(TCBlocks.ELDRITCH_CAPSTONE.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TCBlocks.OBSIDIAN_TILE.get())
                .add(TCBlocks.OBSIDIAN_TOTEM.get())
                .add(TCBlocks.OBSIDIAN_TOTEM_CHARGED.get())
                .add(TCBlocks.ELDRITCH_STONE.get())
                .add(TCBlocks.ELDRITCH_STONE_INERT.get())
                .add(TCBlocks.ELDRITCH_ROCK.get())
                .add(TCBlocks.ELDRITCH_CRUST.get())
                .add(TCBlocks.ELDRITCH_CRUST_GLOWING.get())
                .add(TCBlocks.STAIRS_ELDRITCH.get())
                .add(TCBlocks.ELDRITCH_PEDESTAL.get())
                .add(TCBlocks.ELDRITCH_STONE_CRYSTAL.get())
                .add(TCBlocks.ELDRITCH_CRAB_SPAWNER.get())
                .add(TCBlocks.ELDRITCH_TRAP.get());

        tag(BlockTags.RAILS).add(TCBlocks.ACTIVATOR_RAIL.get());

        tag(BlockTags.BEACON_BASE_BLOCKS)
                .add(TCBlocks.METAL_THAUMIUM_BLOCK.get())
                .add(TCBlocks.METAL_BRASS_BLOCK.get())
                .add(TCBlocks.METAL_VOID_BLOCK.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TCBlocks.METAL_THAUMIUM_BLOCK.get())
                .add(TCBlocks.METAL_BRASS_BLOCK.get())
                .add(TCBlocks.METAL_VOID_BLOCK.get())
                .add(TCBlocks.OBSIDIAN_PLACEHOLDER.get())
                .add(TCBlocks.NETHER_BRICKS_PLACEHOLDER.get())
                .add(TCBlocks.INFERNAL_FURNACE.get())
                .add(TCBlocks.ARCANE_BORE.get());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(TCBlocks.METAL_THAUMIUM_BLOCK.get())
                .add(TCBlocks.METAL_BRASS_BLOCK.get())
                .add(TCBlocks.METAL_VOID_BLOCK.get());

        tag(BlockTags.LOGS_THAT_BURN).addTag(TCBlockTags.GREATWOOD_LOGS).addTag(TCBlockTags.SILVERWOOD_LOGS);
        tag(BlockTags.LOGS).addTag(TCBlockTags.GREATWOOD_LOGS).addTag(TCBlockTags.SILVERWOOD_LOGS);

        tag(BlockTags.LEAVES).add(TCBlocks.LEAVES_GREATWOOD.get()).add(TCBlocks.LEAVES_SILVERWOOD.get());

        tag(BlockTags.SAPLINGS).add(TCBlocks.SAPLING_GREATWOOD.get()).add(TCBlocks.SAPLING_SILVERWOOD.get());

        tag(TCBlockTags.PLANKS_GREATWOOD).add(TCBlocks.PLANK_GREATWOOD.get());
        tag(TCBlockTags.PLANKS_SILVERWOOD).add(TCBlocks.PLANK_SILVERWOOD.get());
        tag(BlockTags.PLANKS).addTags(TCBlockTags.PLANKS_GREATWOOD, TCBlockTags.PLANKS_SILVERWOOD);

        tag(BlockTags.MINEABLE_WITH_HOE).add(TCBlocks.LEAVES_GREATWOOD.get()).add(TCBlocks.LEAVES_SILVERWOOD.get());

        tag(TCBlockTags.CRUCIBLE_HEAT_SOURCES)
                .add(Blocks.LAVA)
                .add(Blocks.FIRE)
                .add(Blocks.CAMPFIRE)
                .add(Blocks.SOUL_FIRE)
                .add(Blocks.SOUL_CAMPFIRE)
                .add(Blocks.MAGMA_BLOCK)
                .add(TCBlocks.NITORS.values().stream().map(DeferredHolder::get).toArray(Block[]::new));

        tag(TCBlockTags.SCAN_CLAY).add(Blocks.CLAY).addTag(BlockTags.TERRACOTTA);

        tag(TCBlockTags.ORES_AMBER).add(TCBlocks.ORE_AMBER.get());
        tag(TCBlockTags.ORES_CINNABAR).add(TCBlocks.ORE_CINNABAR.get());
        tag(Tags.Blocks.ORES_QUARTZ).add(TCBlocks.ORE_QUARTZ.get());
        tag(Tags.Blocks.ORES).addTags(TCBlockTags.ORES_AMBER, TCBlockTags.ORES_CINNABAR, TCBlockTags.ORES_QUARTZ);

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TCBlocks.ORE_AMBER.get())
                .add(TCBlocks.ORE_CINNABAR.get())
                .add(TCBlocks.ORE_QUARTZ.get())
                .add(TCBlocks.SMELTER_BASIC.get())
                .add(TCBlocks.SMELTER_THAUMIUM.get())
                .add(TCBlocks.SMELTER_VOID.get())
                .add(TCBlocks.SMELTER_AUX.get())
                .add(TCBlocks.SMELTER_VENT.get())
                .add(TCBlocks.SPA.get())
                .add(TCBlocks.ARCANE_WORKBENCH_CHARGER.get())
                .add(TCBlocks.PEDESTAL_ARCANE.get())
                .add(TCBlocks.PEDESTAL_ANCIENT.get())
                .add(TCBlocks.PEDESTAL_ELDRITCH.get())
                .add(TCBlocks.ALCHEMICAL_CONSTRUCT.get())
                .add(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TCBlocks.INFUSION_MATRIX.get())
                .add(TCBlocks.MATRIX_SPEED.get())
                .add(TCBlocks.MATRIX_COST.get())
                .add(TCBlocks.STABILIZER.get())
                .add(TCBlocks.STONE_ARCANE.get())
                .add(TCBlocks.STONE_ARCANE_BRICK.get())
                .add(TCBlocks.STONE_ANCIENT.get())
                .add(TCBlocks.STONE_ANCIENT_TILE.get())
                .add(TCBlocks.STONE_ANCIENT_GLYPHED.get())
                .add(TCBlocks.STONE_ELDRITCH_TILE.get())
                .add(TCBlocks.STONE_POROUS.get())
                .add(TCBlocks.STAIRS_ARCANE.get())
                .add(TCBlocks.STAIRS_ARCANE_BRICK.get())
                .add(TCBlocks.STAIRS_ANCIENT.get())
                .add(TCBlocks.PILLAR_ARCANE.get())
                .add(TCBlocks.PILLAR_ANCIENT.get())
                .add(TCBlocks.PILLAR_ELDRITCH.get())
                .add(TCBlocks.RECHARGE_PEDESTAL.get());

        tag(BlockTags.NEEDS_STONE_TOOL).add(TCBlocks.ORE_AMBER.get());

        tag(BlockTags.NEEDS_IRON_TOOL).add(TCBlocks.ORE_CINNABAR.get());

        tag(TCBlockTags.PORTABLE_HOLE_BLACKLIST);

        tag(TCBlockTags.STORAGE_BLOCKS_AMBER).add(TCBlocks.AMBER_BLOCK.get());
        tag(TCBlockTags.STORAGE_BLOCKS_BRASS).add(TCBlocks.METAL_BRASS_BLOCK.get());
        tag(TCBlockTags.STORAGE_BLOCKS_THAUMIUM).add(TCBlocks.METAL_THAUMIUM_BLOCK.get());
        tag(TCBlockTags.STORAGE_BLOCKS_VOID_METAL).add(TCBlocks.METAL_VOID_BLOCK.get());
        tag(Tags.Blocks.STORAGE_BLOCKS)
                .addTags(
                        TCBlockTags.STORAGE_BLOCKS_AMBER,
                        TCBlockTags.STORAGE_BLOCKS_BRASS,
                        TCBlockTags.STORAGE_BLOCKS_THAUMIUM,
                        TCBlockTags.STORAGE_BLOCKS_VOID_METAL);

        tag(TCBlockTags.GREATWOOD_LOGS)
                .add(TCBlocks.LOG_GREATWOOD.get())
                .add(TCBlocks.WOOD_GREATWOOD.get())
                .add(TCBlocks.STRIPPED_LOG_GREATWOOD.get())
                .add(TCBlocks.STRIPPED_WOOD_GREATWOOD.get());

        tag(TCBlockTags.SILVERWOOD_LOGS)
                .add(TCBlocks.LOG_SILVERWOOD.get())
                .add(TCBlocks.WOOD_SILVERWOOD.get())
                .add(TCBlocks.STRIPPED_LOG_SILVERWOOD.get())
                .add(TCBlocks.STRIPPED_WOOD_SILVERWOOD.get());

        tag(BlockTags.OVERWORLD_NATURAL_LOGS).add(TCBlocks.LOG_GREATWOOD.get()).add(TCBlocks.LOG_SILVERWOOD.get());

        tag(BlockTags.SNAPS_GOAT_HORN).add(TCBlocks.LOG_GREATWOOD.get()).add(TCBlocks.LOG_SILVERWOOD.get());

        tag(BlockTags.FLOWERS).add(TCBlocks.PLANT_SHIMMERLEAF.get()).add(TCBlocks.PLANT_CINDERPEARL.get());

        tag(TCBlockTags.MAGICAL_PLANTS)
                .add(TCBlocks.PLANT_SHIMMERLEAF.get())
                .add(TCBlocks.PLANT_CINDERPEARL.get())
                .add(TCBlocks.PLANT_VISHROOM.get());

        tag(Tags.Blocks.STRIPPED_LOGS)
                .add(TCBlocks.STRIPPED_LOG_GREATWOOD.get())
                .add(TCBlocks.STRIPPED_LOG_SILVERWOOD.get());

        tag(Tags.Blocks.STRIPPED_WOODS)
                .add(TCBlocks.STRIPPED_WOOD_GREATWOOD.get())
                .add(TCBlocks.STRIPPED_WOOD_SILVERWOOD.get());
    }
}
