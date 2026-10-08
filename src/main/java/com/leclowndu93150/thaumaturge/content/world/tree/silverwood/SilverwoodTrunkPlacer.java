package com.leclowndu93150.thaumaturge.content.world.tree.silverwood;

import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.world.tree.TreeLeafUpdater;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public final class SilverwoodTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<SilverwoodTrunkPlacer> CODEC =
            RecordCodecBuilder.mapCodec(instance -> trunkPlacerParts(instance)
                    .and(instance.group(
                            Codec.BOOL.fieldOf("grow_nodes").forGetter(placer -> placer.growNodes),
                            Codec.BOOL.fieldOf("keep_apart").forGetter(placer -> placer.keepApart)))
                    .apply(instance, SilverwoodTrunkPlacer::new));

    private static final int CROWN_BELOW_TOP = 5;
    private static final int CROWN_ABOVE_TOP = 3;
    private static final int CROWN_EXTRA_HEIGHT = 3;
    private static final int CROWN_CORE_BELOW_TOP = 3;
    private static final int CROWN_RADIUS = 5;
    private static final int CROWN_DENSITY_BASE = 10;
    private static final int CROWN_DENSITY_SPREAD = 8;
    private static final int SKIRT_SPREAD = 3;
    private static final int SKIRT_DEPTH = 2;
    private static final float NODE_SPACING = 1.5F;
    private static final int OPTIONAL_LOG_ONE_IN = 3;
    private static final int BOUGH_DROP = 4;
    private static final int APART_RADIUS = 12;
    private static final int APART_BELOW = 8;
    private static final int APART_ABOVE = 8;
    private static final int CANOPY_PROBE_RADIUS = 5;
    private static final int CANOPY_PROBE_BELOW_TOP = 5;
    private static final int CANOPY_PROBE_ABOVE_TOP = 5;
    private static final int TRUNK_PROBE_RADIUS_SQ = 4;
    private static final int[][] DIAGONALS = {{-1, -1}, {1, 1}, {-1, 1}, {1, -1}};

    private final boolean growNodes;
    private final boolean keepApart;

    public SilverwoodTrunkPlacer(
            int baseHeight, int heightRandA, int heightRandB, boolean growNodes, boolean keepApart) {
        super(baseHeight, heightRandA, heightRandB);
        this.growNodes = growNodes;
        this.keepApart = keepApart;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TTTreePlacers.SILVERWOOD_TRUNK.get();
    }

    @Override
    public int getTreeHeight(RandomSource random) {
        int height = baseHeight + random.nextInt(heightRandA + 1);
        return heightRandB > 0 ? height + random.nextInt(heightRandB + 1) : height;
    }

    @Override
    public boolean isFree(LevelSimulatedReader level, BlockPos pos) {
        return true;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
            LevelSimulatedReader reader,
            BiConsumer<BlockPos, BlockState> trunkSetter,
            RandomSource random,
            int treeHeight,
            BlockPos origin,
            TreeConfiguration config) {
        if (!(reader instanceof WorldGenLevel level)) {
            return List.of();
        }
        Block log = config.trunkProvider.getState(random, origin).getBlock();
        if (origin.getY() + treeHeight + 1 > level.getMaxBuildHeight()
                || !hasRoom(level, origin, treeHeight)
                || !level.getFluidState(origin).isEmpty()
                || !isSoil(level.getBlockState(origin.below()))) {
            return List.of();
        }
        if (keepApart && (siblingNearby(level, origin, treeHeight, log) || canopyCrowded(level, origin, treeHeight))) {
            return List.of();
        }
        BlockState leaves = config.foliageProvider.getState(random, origin);
        List<FoliagePlacer.FoliageAttachment> crown = sowCrown(level, random, origin, treeHeight, leaves);
        raiseTrunk(level, trunkSetter, random, config, origin, treeHeight);
        spreadRoots(level, trunkSetter, random, config, origin);
        spreadBoughs(level, trunkSetter, random, config, origin.above(treeHeight - BOUGH_DROP));
        return crown;
    }

    private static boolean hasRoom(WorldGenLevel level, BlockPos origin, int height) {
        int top = origin.getY() + 1 + height;
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        for (int y = origin.getY(); y <= top; y++) {
            int spread = y == origin.getY() ? 0 : y >= top - SKIRT_DEPTH ? SKIRT_SPREAD : 1;
            for (int dx = -spread; dx <= spread; dx++) {
                for (int dz = -spread; dz <= spread; dz++) {
                    if (y < level.getMinBuildHeight() || y > level.getMaxBuildHeight()) {
                        return false;
                    }
                    BlockState state = level.getBlockState(probe.set(origin.getX() + dx, y, origin.getZ() + dz));
                    if (y > origin.getY() && !state.isAir() && !state.is(BlockTags.LEAVES) && !state.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isSoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND);
    }

    private static boolean siblingNearby(WorldGenLevel level, BlockPos origin, int height, Block log) {
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        for (int dx = -APART_RADIUS; dx <= APART_RADIUS; dx++) {
            for (int dz = -APART_RADIUS; dz <= APART_RADIUS; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                for (int dy = -APART_BELOW; dy <= height + APART_ABOVE; dy++) {
                    if (level.getBlockState(probe.setWithOffset(origin, dx, dy, dz))
                            .is(log)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean canopyCrowded(WorldGenLevel level, BlockPos origin, int height) {
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        int canopyFloor = height - CANOPY_PROBE_BELOW_TOP;
        for (int dx = -CANOPY_PROBE_RADIUS; dx <= CANOPY_PROBE_RADIUS; dx++) {
            for (int dz = -CANOPY_PROBE_RADIUS; dz <= CANOPY_PROBE_RADIUS; dz++) {
                for (int dy = -1; dy <= height + CANOPY_PROBE_ABOVE_TOP; dy++) {
                    if (dy < canopyFloor && dx * dx + dz * dz > TRUNK_PROBE_RADIUS_SQ) {
                        continue;
                    }
                    BlockState state = level.getBlockState(probe.setWithOffset(origin, dx, dy, dz));
                    if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static List<FoliagePlacer.FoliageAttachment> sowCrown(
            WorldGenLevel level, RandomSource random, BlockPos origin, int height, BlockState leaves) {
        List<FoliagePlacer.FoliageAttachment> cells = new ArrayList<>();
        int top = origin.getY() + height;
        int crownTop = top + CROWN_ABOVE_TOP + random.nextInt(CROWN_EXTRA_HEIGHT);
        for (int y = top - CROWN_BELOW_TOP; y <= crownTop; y++) {
            int coreY = Mth.clamp(y, top - CROWN_CORE_BELOW_TOP, top);
            for (int dx = -CROWN_RADIUS; dx <= CROWN_RADIUS; dx++) {
                for (int dz = -CROWN_RADIUS; dz <= CROWN_RADIUS; dz++) {
                    double rise = y - coreY;
                    double reachSq = (double) dx * dx + rise * rise + (double) dz * dz;
                    BlockPos cell = new BlockPos(origin.getX() + dx, y, origin.getZ() + dz);
                    BlockState present = level.getBlockState(cell);
                    if (reachSq < CROWN_DENSITY_BASE + random.nextInt(CROWN_DENSITY_SPREAD)
                            && !present.is(leaves.getBlock())) {
                        if (present.is(BlockTags.LEAVES)) {
                            level.setBlock(
                                    cell,
                                    TreeLeafUpdater.carryDistance(leaves, present),
                                    Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE);
                            cells.add(new FoliagePlacer.FoliageAttachment(cell, 0, false));
                        } else if (present.isAir() || present.canBeReplaced()) {
                            level.setBlock(cell, leaves, Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE);
                            cells.add(new FoliagePlacer.FoliageAttachment(cell, 0, false));
                        }
                    }
                }
            }
        }
        return cells;
    }

    private void raiseTrunk(
            WorldGenLevel level,
            BiConsumer<BlockPos, BlockState> trunkSetter,
            RandomSource random,
            TreeConfiguration config,
            BlockPos origin,
            int height) {
        int nodeOdds = Math.max(1, (int) (height * NODE_SPACING));
        boolean previousWasNode = false;
        for (int rise = 0; rise < height; rise++) {
            BlockPos core = origin.above(rise);
            if (!isClearing(level.getBlockState(core))) {
                continue;
            }
            boolean nodeHere = growNodes && rise > 0 && !previousWasNode && random.nextInt(nodeOdds) == 0;
            if (nodeHere) {
                trunkSetter.accept(
                        core,
                        TTBlocks.SILVERWOOD_NODE_LOG
                                .get()
                                .defaultBlockState()
                                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                NodeGenerator.createRandomNodeAt(
                        level,
                        core,
                        random,
                        true,
                        false,
                        false,
                        NodeGenerator.DEFAULT_SPECIAL_RARITY,
                        NodeGenerator.DEFAULT_BASE_AURA);
                nodeOdds += height;
            } else {
                log(level, trunkSetter, random, config, core, Direction.Axis.Y);
            }
            previousWasNode = nodeHere;
            for (Direction side : Direction.Plane.HORIZONTAL) {
                log(level, trunkSetter, random, config, core.relative(side), Direction.Axis.Y);
            }
        }
        log(level, trunkSetter, random, config, origin.above(height), Direction.Axis.Y);
    }

    private void spreadRoots(
            WorldGenLevel level,
            BiConsumer<BlockPos, BlockState> trunkSetter,
            RandomSource random,
            TreeConfiguration config,
            BlockPos origin) {
        for (int[] diagonal : DIAGONALS) {
            log(level, trunkSetter, random, config, origin.offset(diagonal[0], 0, diagonal[1]), Direction.Axis.Y);
        }
        for (int[] diagonal : DIAGONALS) {
            if (random.nextInt(OPTIONAL_LOG_ONE_IN) != 0) {
                log(level, trunkSetter, random, config, origin.offset(diagonal[0], 1, diagonal[1]), Direction.Axis.Y);
            }
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            log(level, trunkSetter, random, config, origin.relative(side, 2), side.getAxis());
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            log(level, trunkSetter, random, config, origin.below().relative(side, 2), Direction.Axis.Y);
        }
    }

    private void spreadBoughs(
            WorldGenLevel level,
            BiConsumer<BlockPos, BlockState> trunkSetter,
            RandomSource random,
            TreeConfiguration config,
            BlockPos fork) {
        for (int[] diagonal : DIAGONALS) {
            log(level, trunkSetter, random, config, fork.offset(diagonal[0], 0, diagonal[1]), Direction.Axis.Y);
        }
        for (int[] diagonal : DIAGONALS) {
            if (random.nextInt(OPTIONAL_LOG_ONE_IN) == 0) {
                log(level, trunkSetter, random, config, fork.offset(diagonal[0], -1, diagonal[1]), Direction.Axis.Y);
            }
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            log(level, trunkSetter, random, config, fork.relative(side, 2), side.getAxis());
        }
    }

    private static boolean isClearing(BlockState state) {
        return state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced();
    }

    private static void log(
            WorldGenLevel level,
            BiConsumer<BlockPos, BlockState> trunkSetter,
            RandomSource random,
            TreeConfiguration config,
            BlockPos pos,
            Direction.Axis axis) {
        if (isClearing(level.getBlockState(pos))) {
            trunkSetter.accept(
                    pos, config.trunkProvider.getState(random, pos).trySetValue(RotatedPillarBlock.AXIS, axis));
        }
    }
}
