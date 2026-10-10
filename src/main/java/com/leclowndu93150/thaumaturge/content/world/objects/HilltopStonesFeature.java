package com.leclowndu93150.thaumaturge.content.world.objects;

import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

public final class HilltopStonesFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_PLATFORM_Y = 85;
    private static final int PLATFORM_RADIUS = 3;
    private static final int SAMPLE_CORNER_REACH = PLATFORM_RADIUS - 1;
    private static final int MAX_GROUND_RISE = 2;
    private static final int MAX_COVER_LAYERS = 2;
    private static final int FOOTING_DEPTH = 4;
    private static final int PILLAR_MIN_HEIGHT = 2;
    private static final int PILLAR_MAX_HEIGHT = 4;
    private static final int PILLAR_SIDE_SHIFT = 1;
    private static final int VINE_ONE_IN = 3;
    private static final int VINE_MAX_DROP = 4;
    private static final int NODE_HEIGHT = 5;
    private static final int CHEST_HEIGHT = 2;
    private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS;

    public HilltopStonesFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BlockPos anchor = groundAt(level, origin.getX(), origin.getZ());
        if (anchor == null || anchor.getY() + 1 < MIN_PLATFORM_Y || !siteIsLevel(level, anchor)) {
            return false;
        }
        BlockPos centre = anchor.above();
        layPlatform(level, random, centre, level.getBlockState(anchor));
        boolean snowy = level.getBiome(centre).value().coldEnoughToSnow(centre, level.getSeaLevel());
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos edge = centre.relative(side, PLATFORM_RADIUS).above();
            raisePillar(level, random, edge.relative(side.getClockWise(), PILLAR_SIDE_SHIFT), snowy);
            raisePillar(level, random, edge.relative(side.getCounterClockWise(), PILLAR_SIDE_SHIFT), snowy);
        }
        furnishCentre(level, random, centre);
        NodeGenerator.createRandomNodeAt(level, centre.above(NODE_HEIGHT), random, false, false, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA);
        return true;
    }

    private static @Nullable BlockPos groundAt(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1, z);
        for (int layer = 0; layer < MAX_COVER_LAYERS && isCover(level.getBlockState(probe)); layer++) {
            probe.move(Direction.DOWN);
        }
        BlockState ground = level.getBlockState(probe);
        return ground.is(BlockTags.DIRT) || ground.is(BlockTags.GRASS_BLOCKS) || ground.is(BlockTags.BASE_STONE_OVERWORLD) ? probe.immutable() : null;
    }

    private static boolean isCover(BlockState state) {
        return state.is(Blocks.SNOW) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN);
    }

    private static boolean siteIsLevel(WorldGenLevel level, BlockPos anchor) {
        for (int stepX = -1; stepX <= 1; stepX++) {
            for (int stepZ = -1; stepZ <= 1; stepZ++) {
                int reach = stepX != 0 && stepZ != 0 ? SAMPLE_CORNER_REACH : PLATFORM_RADIUS;
                BlockPos sample = groundAt(level, anchor.getX() + stepX * reach, anchor.getZ() + stepZ * reach);
                if (sample == null) {
                    return false;
                }
                int rise = sample.getY() - anchor.getY();
                if (rise < 0 || rise > MAX_GROUND_RISE) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void layPlatform(WorldGenLevel level, RandomSource random, BlockPos centre, BlockState footing) {
        BlockState tile = TTBlocks.OBSIDIAN_TILE.get().defaultBlockState();
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        for (int dx = -PLATFORM_RADIUS; dx <= PLATFORM_RADIUS; dx++) {
            for (int dz = -PLATFORM_RADIUS; dz <= PLATFORM_RADIUS; dz++) {
                if (Math.abs(dx) == PLATFORM_RADIUS && Math.abs(dz) == PLATFORM_RADIUS) {
                    continue;
                }
                BlockPos cell = centre.offset(dx, 0, dz);
                level.setBlock(cell, random.nextBoolean() ? tile : obsidian, PLACE_FLAGS);
                fillBeneath(level, cell, footing);
            }
        }
    }

    private static void fillBeneath(WorldGenLevel level, BlockPos cell, BlockState footing) {
        for (int depth = 1; depth <= FOOTING_DEPTH; depth++) {
            BlockPos below = cell.below(depth);
            BlockState existing = level.getBlockState(below);
            if (!existing.isAir() && !isCover(existing) && !existing.is(BlockTags.SMALL_FLOWERS)) {
                return;
            }
            level.setBlock(below, footing, PLACE_FLAGS);
        }
    }

    private static void raisePillar(WorldGenLevel level, RandomSource random, BlockPos base, boolean snowy) {
        BlockState totem = TTBlocks.OBSIDIAN_TOTEM.get().defaultBlockState();
        int height = 0;
        boolean endedOnRoll = false;
        while (height < PILLAR_MAX_HEIGHT && !endedOnRoll) {
            level.setBlock(base.above(height), totem, PLACE_FLAGS);
            height++;
            endedOnRoll = height >= PILLAR_MIN_HEIGHT && random.nextBoolean();
        }
        if (endedOnRoll && !snowy) {
            drapeVines(level, random, base.above(height - 1));
        }
        ObsidianTotemFeature.reshapeTotems(level, base, PILLAR_MAX_HEIGHT);
    }

    private static void drapeVines(WorldGenLevel level, RandomSource random, BlockPos top) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos start = top.relative(side);
            if (!level.isEmptyBlock(start) || random.nextInt(VINE_ONE_IN) != 0) {
                continue;
            }
            BlockState vine = Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(side.getOpposite()), true);
            BlockPos.MutableBlockPos strand = start.mutable();
            for (int drop = 0; drop <= VINE_MAX_DROP && level.isEmptyBlock(strand); drop++) {
                level.setBlock(strand, vine, PLACE_FLAGS);
                strand.move(Direction.DOWN);
            }
        }
    }

    private static void furnishCentre(WorldGenLevel level, RandomSource random, BlockPos centre) {
        level.setBlock(centre, Blocks.SPAWNER.defaultBlockState(), PLACE_FLAGS);
        if (level.getBlockEntity(centre) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(TTEntities.WISP.get(), random);
        }
        level.setBlock(centre.above(), TTBlocks.OBSIDIAN_TILE.get().defaultBlockState(), PLACE_FLAGS);
        BlockPos chest = centre.above(CHEST_HEIGHT);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState(), PLACE_FLAGS);
        RandomizableContainer.setBlockEntityLootTable(level, random, chest, BuiltInLootTables.SIMPLE_DUNGEON);
    }
}
