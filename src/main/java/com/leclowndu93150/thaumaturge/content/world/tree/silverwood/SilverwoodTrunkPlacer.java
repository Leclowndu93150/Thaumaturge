package com.leclowndu93150.thaumaturge.content.world.tree.silverwood;

import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
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
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public final class SilverwoodTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<SilverwoodTrunkPlacer> CODEC = RecordCodecBuilder
            .mapCodec(instance -> trunkPlacerParts(instance).and(Codec.BOOL.fieldOf("grow_nodes").forGetter(placer -> placer.growNodes))
                    .and(Codec.BOOL.fieldOf("keep_apart").forGetter(placer -> placer.keepApart)).apply(instance, SilverwoodTrunkPlacer::new));

    private static final int PLACE_FLAGS = Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE;
    private static final int CROWN_DROP = 5;
    private static final int CORE_DROP = 3;
    private static final int CROWN_MIN_RISE = 3;
    private static final int CROWN_RISE_SPREAD = 2;
    private static final double CORE_RADIUS = 3.2;
    private static final double EDGE_JITTER = 0.9;
    private static final int CROWN_SCAN_RADIUS = 4;
    private static final int NARROW_ROOM_RADIUS = 1;
    private static final int WIDE_ROOM_RADIUS = 3;
    private static final int WIDE_ROOM_LAYERS = 3;
    private static final int BOUGH_DROP = 4;
    private static final int SIDE_LOG_REACH = 2;
    private static final int FLARE_STACK_ODDS = 3;
    private static final int BOUGH_DROOP_ODDS = 3;
    private static final double FIRST_NODE_ODDS = 1.5;
    private static final double NODE_ODDS_STEP = 1.0;
    private static final int KEEP_APART_RADIUS = 8;

    private final boolean growNodes;
    private final boolean keepApart;

    public SilverwoodTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, boolean growNodes, boolean keepApart) {
        super(baseHeight, heightRandA, heightRandB);
        this.growNodes = growNodes;
        this.keepApart = keepApart;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TTTreePlacers.SILVERWOOD_TRUNK.get();
    }

    @Override
    public boolean isFree(WorldGenLevel level, BlockPos pos) {
        return level.isStateAtPosition(pos, SilverwoodTrunkPlacer::isRoom);
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, int treeHeight, BlockPos origin, TreeConfiguration config) {
        int rise = CROWN_MIN_RISE + random.nextInt(CROWN_RISE_SPREAD + 1);
        if (!fitsBuildHeight(level, origin, treeHeight, rise) || !standsOnSoil(level, origin) || !hasRoom(level, origin, treeHeight)
                || (keepApart && nearAnotherSilverwood(level, origin, treeHeight))) {
            return List.of();
        }
        List<FoliagePlacer.FoliageAttachment> crown = growCrown(level, random, config, origin, treeHeight, rise);
        raiseTrunk(level, trunkSetter, random, config, origin, treeHeight);
        raiseCore(level, trunkSetter, random, config, origin, treeHeight);
        spreadRootFlare(level, trunkSetter, random, config, origin);
        spreadBoughs(level, trunkSetter, random, config, origin.above(treeHeight - BOUGH_DROP));
        return crown;
    }

    private static boolean isRoom(BlockState state) {
        return state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced();
    }

    private static boolean isSoil(BlockState state) {
        return state.is(BlockTags.SUPPORTS_VEGETATION);
    }

    private static boolean fitsBuildHeight(WorldGenLevel level, BlockPos origin, int treeHeight, int rise) {
        return !level.isOutsideBuildHeight(origin.getY() - 1) && !level.isOutsideBuildHeight(origin.getY() + treeHeight + rise);
    }

    private static boolean standsOnSoil(WorldGenLevel level, BlockPos origin) {
        BlockPos ground = origin.below();
        if (!level.isStateAtPosition(ground, SilverwoodTrunkPlacer::isSoil)) {
            return false;
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (!level.isStateAtPosition(ground.relative(side), SilverwoodTrunkPlacer::isSoil)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasRoom(WorldGenLevel level, BlockPos origin, int treeHeight) {
        int highest = treeHeight + 1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy <= highest; dy++) {
            int radius = dy > highest - WIDE_ROOM_LAYERS ? WIDE_ROOM_RADIUS : NARROW_ROOM_RADIUS;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    cursor.setWithOffset(origin, dx, dy, dz);
                    if (!isRoom(level.getBlockState(cursor))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean nearAnotherSilverwood(WorldGenLevel level, BlockPos origin, int treeHeight) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy <= treeHeight; dy++) {
            for (int dx = -KEEP_APART_RADIUS; dx <= KEEP_APART_RADIUS; dx++) {
                for (int dz = -KEEP_APART_RADIUS; dz <= KEEP_APART_RADIUS; dz++) {
                    cursor.setWithOffset(origin, dx, dy, dz);
                    if (level.getBlockState(cursor).is(TTBlockTags.SILVERWOOD_LOGS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static List<FoliagePlacer.FoliageAttachment> growCrown(WorldGenLevel level, RandomSource random, TreeConfiguration config, BlockPos origin, int treeHeight, int rise) {
        List<FoliagePlacer.FoliageAttachment> leaves = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int fromTop = -CROWN_DROP; fromTop <= rise; fromTop++) {
            double radius = layerRadius(fromTop, rise);
            for (int dx = -CROWN_SCAN_RADIUS; dx <= CROWN_SCAN_RADIUS; dx++) {
                for (int dz = -CROWN_SCAN_RADIUS; dz <= CROWN_SCAN_RADIUS; dz++) {
                    double cutOff = radius + random.nextDouble() * EDGE_JITTER;
                    if (dx * dx + dz * dz > cutOff * cutOff) {
                        continue;
                    }
                    cursor.setWithOffset(origin, dx, treeHeight + fromTop, dz);
                    if (TreeFeature.validTreePos(level, cursor)) {
                        BlockPos leaf = cursor.immutable();
                        level.setBlock(leaf, config.foliageProvider.getState(level, random, leaf), PLACE_FLAGS);
                        leaves.add(new FoliagePlacer.FoliageAttachment(leaf, 0, false));
                    }
                }
            }
        }
        return leaves;
    }

    private static double layerRadius(int fromTop, int rise) {
        if (fromTop > 0) {
            return capRadius(fromTop, rise);
        }
        if (fromTop < -CORE_DROP) {
            return capRadius(-CORE_DROP - fromTop, CROWN_DROP - CORE_DROP);
        }
        return CORE_RADIUS;
    }

    private static double capRadius(int depth, int capHeight) {
        double share = depth / (capHeight + 1.0);
        return CORE_RADIUS * Math.sqrt(1.0 - share * share);
    }

    private static void raiseTrunk(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, BlockPos origin, int treeHeight) {
        setLog(level, trunkSetter, random, config, origin, Direction.Axis.Y);
        for (int dy = 0; dy < treeHeight; dy++) {
            BlockPos layer = origin.above(dy);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                setLog(level, trunkSetter, random, config, layer.relative(side), Direction.Axis.Y);
            }
        }
    }

    private void raiseCore(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, BlockPos origin, int treeHeight) {
        int nodesGrown = 0;
        boolean lastWasNode = false;
        for (int dy = 1; dy <= treeHeight; dy++) {
            BlockPos pos = origin.above(dy);
            if (growNodes && !lastWasNode && rollsNode(random, treeHeight, nodesGrown)) {
                lastWasNode = plantNode(level, trunkSetter, random, config, pos);
                if (lastWasNode) {
                    nodesGrown++;
                }
            } else {
                lastWasNode = false;
                setLog(level, trunkSetter, random, config, pos, Direction.Axis.Y);
            }
        }
    }

    private static boolean rollsNode(RandomSource random, int treeHeight, int nodesGrown) {
        int odds = Math.max(1, Mth.floor((FIRST_NODE_ODDS + nodesGrown * NODE_ODDS_STEP) * treeHeight));
        return random.nextInt(odds) == 0;
    }

    private static boolean plantNode(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, BlockPos pos) {
        if (!level.isStateAtPosition(pos, SilverwoodTrunkPlacer::isRoom)) {
            return false;
        }
        trunkSetter.accept(pos, TTBlocks.SILVERWOOD_NODE_LOG.get().defaultBlockState());
        if (NodeGenerator.createRandomNodeAt(level, pos, random, true, false, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA)) {
            return true;
        }
        trunkSetter.accept(pos, logState(level, random, config, pos, Direction.Axis.Y));
        return false;
    }

    private static void spreadRootFlare(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, BlockPos origin) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos diagonal = origin.relative(side).relative(side.getClockWise());
            setLog(level, trunkSetter, random, config, diagonal, Direction.Axis.Y);
            if (random.nextInt(FLARE_STACK_ODDS) != 0) {
                setLog(level, trunkSetter, random, config, diagonal.above(), Direction.Axis.Y);
            }
            BlockPos root = origin.relative(side, SIDE_LOG_REACH);
            setLog(level, trunkSetter, random, config, root, side.getAxis());
            BlockPos anchor = root.below();
            if (level.isStateAtPosition(anchor, state -> isRoom(state) || isSoil(state))) {
                trunkSetter.accept(anchor, logState(level, random, config, anchor, Direction.Axis.Y));
            }
        }
    }

    private static void spreadBoughs(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, BlockPos centre) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos diagonal = centre.relative(side).relative(side.getClockWise());
            setLog(level, trunkSetter, random, config, diagonal, Direction.Axis.Y);
            if (random.nextInt(BOUGH_DROOP_ODDS) == 0) {
                setLog(level, trunkSetter, random, config, diagonal.below(), Direction.Axis.Y);
            }
            setLog(level, trunkSetter, random, config, centre.relative(side, SIDE_LOG_REACH), side.getAxis());
        }
    }

    private static void setLog(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, BlockPos pos, Direction.Axis axis) {
        if (level.isStateAtPosition(pos, SilverwoodTrunkPlacer::isRoom)) {
            trunkSetter.accept(pos, logState(level, random, config, pos, axis));
        }
    }

    private static BlockState logState(WorldGenLevel level, RandomSource random, TreeConfiguration config, BlockPos pos, Direction.Axis axis) {
        return config.trunkProvider.getState(level, random, pos).trySetValue(RotatedPillarBlock.AXIS, axis);
    }
}
