package com.leclowndu93150.thaumaturge.content.world.tree;

import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class SilverwoodTreeFeature extends Feature<SilverwoodTreeConfig> {
    private static final int PLACE_FLAGS = 19;
    private static final int FLOWER_ATTEMPTS = 18;
    private static final int FLOWER_XZ_SPREAD = 8;
    private static final int FLOWER_Y_SPREAD = 4;

    public SilverwoodTreeFeature(Codec<SilverwoodTreeConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SilverwoodTreeConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        SilverwoodTreeConfig config = context.config();
        BlockPos origin = context.origin();
        int height = random.nextInt(config.extraHeight()) + config.minHeight();
        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();
        if (y < level.getMinBuildHeight() + 1 || y + height + 1 > level.getMaxBuildHeight()) {
            return false;
        }

        for (int cy = y; cy <= y + 1 + height; cy++) {
            int spread = 1;
            if (cy == y) {
                spread = 0;
            }
            if (cy >= y + 1 + height - 2) {
                spread = 3;
            }
            for (int cx = x - spread; cx <= x + spread; cx++) {
                for (int cz = z - spread; cz <= z + spread; cz++) {
                    if (cy < level.getMinBuildHeight() || cy > level.getMaxBuildHeight()) {
                        return false;
                    }
                    BlockState state = level.getBlockState(new BlockPos(cx, cy, cz));
                    if (!state.isAir() && !state.is(BlockTags.LEAVES) && !state.canBeReplaced() && cy > y) {
                        return false;
                    }
                }
            }
        }

        if (!level.getFluidState(origin).isEmpty()) {
            return false;
        }
        BlockState soil = level.getBlockState(origin.below());
        if (!soil.is(BlockTags.DIRT) && !soil.is(Blocks.FARMLAND)) {
            return false;
        }

        Set<BlockPos> placedLogs = new HashSet<>();
        Set<BlockPos> placedLeaves = new HashSet<>();
        Set<BlockPos> freshLeaves = new HashSet<>();
        int canopyStart = y + height - 5;
        int canopyEnd = y + height + 3 + random.nextInt(3);
        for (int cy = canopyStart; cy <= canopyEnd; cy++) {
            int clampedY = Mth.clamp(cy, y + height - 3, y + height);
            for (int cx = x - 5; cx <= x + 5; cx++) {
                for (int cz = z - 5; cz <= z + 5; cz++) {
                    double dx = cx - x;
                    double dy = cy - clampedY;
                    double dz = cz - z;
                    double dist = dx * dx + dy * dy + dz * dz;
                    BlockPos pos = new BlockPos(cx, cy, cz);
                    BlockState state = level.getBlockState(pos);
                    if (dist < 10 + random.nextInt(8)) {
                        if (state.is(config.leaves())) {
                            placedLeaves.add(pos);
                        } else if (state.is(BlockTags.LEAVES)) {
                            level.setBlock(
                                    pos,
                                    TreeLeafUpdater.carryDistance(
                                            config.leaves().defaultBlockState(), state),
                                    PLACE_FLAGS);
                            placedLeaves.add(pos);
                        } else if (state.isAir() || state.canBeReplaced()) {
                            level.setBlock(pos, config.leaves().defaultBlockState(), PLACE_FLAGS);
                            placedLeaves.add(pos);
                            freshLeaves.add(pos);
                        }
                    }
                }
            }
        }

        if (config.node()) {
            NodeGenerator.createRandomNodeAt(
                    level,
                    new BlockPos(x, y + height - 1, z),
                    random,
                    true,
                    false,
                    false,
                    NodeGenerator.DEFAULT_SPECIAL_RARITY,
                    NodeGenerator.DEFAULT_BASE_AURA);
        }

        int trunkY;
        for (trunkY = 0; trunkY < height; trunkY++) {
            BlockState state = level.getBlockState(new BlockPos(x, y + trunkY, z));
            if (state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced()) {
                placeLog(level, x, y + trunkY, z, config, placedLogs, Direction.Axis.Y);
                placeLog(level, x - 1, y + trunkY, z, config, placedLogs, Direction.Axis.Y);
                placeLog(level, x + 1, y + trunkY, z, config, placedLogs, Direction.Axis.Y);
                placeLog(level, x, y + trunkY, z - 1, config, placedLogs, Direction.Axis.Y);
                placeLog(level, x, y + trunkY, z + 1, config, placedLogs, Direction.Axis.Y);
            }
        }
        placeLog(level, x, y + trunkY, z, config, placedLogs, Direction.Axis.Y);
        placeLog(level, x - 1, y, z - 1, config, placedLogs, Direction.Axis.Y);
        placeLog(level, x + 1, y, z + 1, config, placedLogs, Direction.Axis.Y);
        placeLog(level, x - 1, y, z + 1, config, placedLogs, Direction.Axis.Y);
        placeLog(level, x + 1, y, z - 1, config, placedLogs, Direction.Axis.Y);
        if (random.nextInt(3) != 0) {
            placeLog(level, x - 1, y + 1, z - 1, config, placedLogs, Direction.Axis.Y);
        }
        if (random.nextInt(3) != 0) {
            placeLog(level, x + 1, y + 1, z + 1, config, placedLogs, Direction.Axis.Y);
        }
        if (random.nextInt(3) != 0) {
            placeLog(level, x - 1, y + 1, z + 1, config, placedLogs, Direction.Axis.Y);
        }
        if (random.nextInt(3) != 0) {
            placeLog(level, x + 1, y + 1, z - 1, config, placedLogs, Direction.Axis.Y);
        }
        placeLog(level, x - 2, y, z, config, placedLogs, Direction.Axis.X);
        placeLog(level, x + 2, y, z, config, placedLogs, Direction.Axis.X);
        placeLog(level, x, y, z - 2, config, placedLogs, Direction.Axis.Z);
        placeLog(level, x, y, z + 2, config, placedLogs, Direction.Axis.Z);
        placeLog(level, x - 2, y - 1, z, config, placedLogs, Direction.Axis.Y);
        placeLog(level, x + 2, y - 1, z, config, placedLogs, Direction.Axis.Y);
        placeLog(level, x, y - 1, z - 2, config, placedLogs, Direction.Axis.Y);
        placeLog(level, x, y - 1, z + 2, config, placedLogs, Direction.Axis.Y);
        placeLog(level, x - 1, y + height - 4, z - 1, config, placedLogs, Direction.Axis.Y);
        placeLog(level, x + 1, y + height - 4, z + 1, config, placedLogs, Direction.Axis.Y);
        placeLog(level, x - 1, y + height - 4, z + 1, config, placedLogs, Direction.Axis.Y);
        placeLog(level, x + 1, y + height - 4, z - 1, config, placedLogs, Direction.Axis.Y);
        if (random.nextInt(3) == 0) {
            placeLog(level, x - 1, y + height - 5, z - 1, config, placedLogs, Direction.Axis.Y);
        }
        if (random.nextInt(3) == 0) {
            placeLog(level, x + 1, y + height - 5, z + 1, config, placedLogs, Direction.Axis.Y);
        }
        if (random.nextInt(3) == 0) {
            placeLog(level, x - 1, y + height - 5, z + 1, config, placedLogs, Direction.Axis.Y);
        }
        if (random.nextInt(3) == 0) {
            placeLog(level, x + 1, y + height - 5, z - 1, config, placedLogs, Direction.Axis.Y);
        }
        placeLog(level, x - 2, y + height - 4, z, config, placedLogs, Direction.Axis.X);
        placeLog(level, x + 2, y + height - 4, z, config, placedLogs, Direction.Axis.X);
        placeLog(level, x, y + height - 4, z - 2, config, placedLogs, Direction.Axis.Z);
        placeLog(level, x, y + height - 4, z + 2, config, placedLogs, Direction.Axis.Z);

        TreeLeafUpdater.run(level, placedLogs, placedLeaves, freshLeaves);
        config.flower().ifPresent(flower -> generateFlowers(level, random, origin, flower));
        return true;
    }

    private static void placeLog(
            WorldGenLevel level,
            int x,
            int y,
            int z,
            SilverwoodTreeConfig config,
            Set<BlockPos> placedLogs,
            Direction.Axis axis) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState existing = level.getBlockState(pos);
        if (existing.isAir() || existing.is(BlockTags.LEAVES) || existing.canBeReplaced()) {
            BlockState state = config.log().defaultBlockState();
            if (state.hasProperty(RotatedPillarBlock.AXIS)) {
                state = state.setValue(RotatedPillarBlock.AXIS, axis);
            }
            level.setBlock(pos, state, PLACE_FLAGS);
            placedLogs.add(pos);
        }
    }

    private static void generateFlowers(WorldGenLevel level, RandomSource random, BlockPos origin, Block flower) {
        for (int i = 0; i < FLOWER_ATTEMPTS; i++) {
            BlockPos pos = origin.offset(
                    random.nextInt(FLOWER_XZ_SPREAD) - random.nextInt(FLOWER_XZ_SPREAD),
                    random.nextInt(FLOWER_Y_SPREAD) - random.nextInt(FLOWER_Y_SPREAD),
                    random.nextInt(FLOWER_XZ_SPREAD) - random.nextInt(FLOWER_XZ_SPREAD));
            BlockState below = level.getBlockState(pos.below());
            if (level.getBlockState(pos).isAir() && (below.is(Blocks.GRASS_BLOCK) || below.is(BlockTags.SAND))) {
                level.setBlock(pos, flower.defaultBlockState(), PLACE_FLAGS);
            }
        }
    }
}
