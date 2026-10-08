package com.leclowndu93150.thaumaturge.content.world.tree.crown;

import net.minecraft.world.level.LevelSimulatedReader;
import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public final class CrownTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<CrownTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> trunkPlacerParts(instance)
            .and(instance.group(CrownShape.CODEC.fieldOf("shape").forGetter(placer -> placer.shape), CrownRule.CODEC.fieldOf("rule").forGetter(placer -> placer.rule)))
            .apply(instance, CrownTrunkPlacer::new));

    private static final int UNOBSTRUCTED = -1;
    private static final int NO_ROOM = 0;
    private static final int MIN_CLEARANCE = 6;
    private static final int CLUSTER_DEPTH = 4;
    private static final double CLUSTER_DENSITY = 0.9;
    private static final double CLUSTERS_PER_LAYER_BASE = 1.382;
    private static final double CLUSTERS_PER_LAYER_SCALE = 13.0;
    private static final double CLUSTER_REACH_FLOOR = 0.328;
    private static final double BRANCHING_FLOOR = 0.2;
    private static final double STACKED_CROWN_WIDTH = 1.66;

    private final CrownShape shape;
    private final CrownRule rule;

    public CrownTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, CrownShape shape, CrownRule rule) {
        super(baseHeight, heightRandA, heightRandB);
        this.shape = shape;
        this.rule = rule;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TTTreePlacers.CROWN_TRUNK.get();
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
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader reader, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, int treeHeight, BlockPos origin, TreeConfiguration config) {
        if (!(reader instanceof WorldGenLevel level)) {
            return List.of();
        }
        Block ownLeaves = config.foliageProvider.getState(random, origin).getBlock();
        int heightLimit = surveyHeight(level, origin, treeHeight, ownLeaves);
        if (heightLimit < MIN_CLEARANCE) {
            return List.of();
        }
        List<FoliagePlacer.FoliageAttachment> clusters = new ArrayList<>();
        int trunkHeight = Math.min((int) (heightLimit * shape.trunkShare()), heightLimit - 1);
        growTier(level, trunkSetter, random, config, origin, heightLimit, trunkHeight, shape.crownWidth(), ownLeaves, clusters);
        if (shape.stackedCrown()) {
            growTier(level, trunkSetter, random, config, origin.above(trunkHeight), heightLimit, trunkHeight, STACKED_CROWN_WIDTH, ownLeaves, clusters);
        }
        return clusters;
    }

    private int surveyHeight(WorldGenLevel level, BlockPos origin, int heightLimit, Block ownLeaves) {
        int limit = heightLimit;
        for (int dx = 0; dx < shape.trunkWidth(); dx++) {
            for (int dz = 0; dz < shape.trunkWidth(); dz++) {
                BlockPos column = origin.offset(dx, 0, dz);
                if (!level.getFluidState(column).isEmpty() || !isSoil(level.getBlockState(column.below()))) {
                    return NO_ROOM;
                }
                int blockedAt = firstObstruction(level, column, column.above(limit - 1), ownLeaves);
                if (blockedAt != UNOBSTRUCTED) {
                    if (blockedAt < MIN_CLEARANCE) {
                        return NO_ROOM;
                    }
                    limit = blockedAt;
                }
            }
        }
        return limit;
    }

    private static boolean isSoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND);
    }

    private void growTier(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, BlockPos foot, int heightLimit, int trunkHeight, double crownWidth, Block ownLeaves, List<FoliagePlacer.FoliageAttachment> clusters) {
        List<CrownNode> nodes = surveyCrown(level, random, foot, heightLimit, foot.getY() + trunkHeight, crownWidth, ownLeaves);
        for (CrownNode node : nodes) {
            if (node.branchFootY() - foot.getY() >= heightLimit * BRANCHING_FLOOR) {
                layLimb(level, trunkSetter, random, config, new BlockPos(foot.getX(), node.branchFootY(), foot.getZ()), node.cluster());
                clusters.add(new FoliagePlacer.FoliageAttachment(node.cluster(), 0, false));
            }
        }
        for (int dx = 0; dx < shape.trunkWidth(); dx++) {
            for (int dz = 0; dz < shape.trunkWidth(); dz++) {
                BlockPos column = foot.offset(dx, 0, dz);
                layLimb(level, trunkSetter, random, config, column, column.above(trunkHeight));
            }
        }
    }

    private List<CrownNode> surveyCrown(WorldGenLevel level, RandomSource random, BlockPos foot, int heightLimit, int branchTop, double crownWidth, Block ownLeaves) {
        List<CrownNode> nodes = new ArrayList<>();
        int topLayer = heightLimit - CLUSTER_DEPTH;
        nodes.add(new CrownNode(foot.above(topLayer), branchTop));
        int perLayer = Math.max(1, (int) (CLUSTERS_PER_LAYER_BASE + Math.pow(CLUSTER_DENSITY * heightLimit / CLUSTERS_PER_LAYER_SCALE, 2.0)));
        for (int layer = topLayer; layer >= 0; layer--) {
            if (rule.belowCrown(layer, heightLimit)) {
                continue;
            }
            float spread = crownSpread(layer, heightLimit);
            for (int attempt = 0; attempt < perLayer; attempt++) {
                double reach = crownWidth * spread * (random.nextFloat() + CLUSTER_REACH_FLOOR);
                double angle = random.nextFloat() * 2.0 * Math.PI;
                BlockPos cluster = new BlockPos(rule.clusterCoordinate(foot.getX(), reach * Math.sin(angle)), foot.getY() + layer - 1, rule.clusterCoordinate(foot.getZ(), reach * Math.cos(angle)));
                if (firstObstruction(level, cluster, cluster.above(CLUSTER_DEPTH), ownLeaves) != UNOBSTRUCTED) {
                    continue;
                }
                int offX = foot.getX() - cluster.getX();
                int offZ = foot.getZ() - cluster.getZ();
                double sag = cluster.getY() - Math.sqrt(offX * offX + offZ * offZ) * shape.branchSlope();
                int branchFoot = sag > branchTop ? branchTop : (int) sag;
                if (firstObstruction(level, new BlockPos(foot.getX(), branchFoot, foot.getZ()), cluster, ownLeaves) == UNOBSTRUCTED) {
                    nodes.add(new CrownNode(cluster, branchFoot));
                }
            }
        }
        return nodes;
    }

    private static float crownSpread(int layer, int heightLimit) {
        float half = heightLimit / 2.0F;
        float fromMiddle = half - layer;
        if (fromMiddle == 0.0F) {
            return half * 0.5F;
        }
        if (Math.abs(fromMiddle) >= half) {
            return 0.0F;
        }
        return (float) Math.sqrt(half * half - fromMiddle * fromMiddle) * 0.5F;
    }

    private int firstObstruction(WorldGenLevel level, BlockPos from, BlockPos to, Block ownLeaves) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        if (steps == 0) {
            return UNOBSTRUCTED;
        }
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        for (int index = 0; index <= steps; index++) {
            probe.set(rule.lineCoordinate(from.getX(), dx, steps, index, true), rule.lineCoordinate(from.getY(), dy, steps, index, true), rule.lineCoordinate(from.getZ(), dz, steps, index, true));
            if (!rule.isOpen(level.getBlockState(probe), ownLeaves)) {
                return index;
            }
        }
        return UNOBSTRUCTED;
    }

    private void layLimb(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        if (steps == 0) {
            return;
        }
        for (int index = 0; index <= steps; index++) {
            BlockPos pos = new BlockPos(rule.lineCoordinate(from.getX(), dx, steps, index, false), rule.lineCoordinate(from.getY(), dy, steps, index, false),
                    rule.lineCoordinate(from.getZ(), dz, steps, index, false));
            if (CrownRule.canHostLog(level.getBlockState(pos))) {
                trunkSetter.accept(pos, config.trunkProvider.getState(random, pos).trySetValue(RotatedPillarBlock.AXIS, grainAxis(from, pos)));
            }
        }
    }

    private static Direction.Axis grainAxis(BlockPos from, BlockPos to) {
        int runX = Math.abs(to.getX() - from.getX());
        int runZ = Math.abs(to.getZ() - from.getZ());
        int run = Math.max(runX, runZ);
        if (run == 0) {
            return Direction.Axis.Y;
        }
        return runX == run ? Direction.Axis.X : Direction.Axis.Z;
    }
}
