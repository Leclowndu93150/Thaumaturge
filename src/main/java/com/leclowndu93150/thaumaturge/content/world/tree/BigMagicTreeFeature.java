package com.leclowndu93150.thaumaturge.content.world.tree;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class BigMagicTreeFeature extends Feature<BigMagicTreeConfig> {
    private static final double HEIGHT_ATTENUATION = 0.6618;
    private static final double BRANCH_SLOPE = 0.381;
    private static final double SCALE_WIDTH = 1.25;
    private static final double LEAF_DENSITY = 0.9;
    private static final int HEIGHT_LIMIT_LIMIT = 11;
    private static final int LEAF_DISTANCE_LIMIT = 4;
    private static final int MIN_CLEARANCE = 6;
    private static final int PLACE_FLAGS = 19;

    public BigMagicTreeFeature(Codec<BigMagicTreeConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BigMagicTreeConfig> context) {
        return new Generator(context.level(), context.random(), context.config(), context.origin()).generate();
    }

    private record FoliageNode(BlockPos pos, int branchBase) {}

    private static final class Generator {
        private final WorldGenLevel level;
        private final RandomSource rand;
        private final BigMagicTreeConfig config;
        private final BlockPos basePos;
        private final Set<BlockPos> placedLogs = new HashSet<>();
        private final Set<BlockPos> placedLeaves = new HashSet<>();
        private final Set<BlockPos> freshLeaves = new HashSet<>();
        private int heightLimit;
        private int height;
        private List<FoliageNode> foliageCoords;

        Generator(WorldGenLevel level, RandomSource rand, BigMagicTreeConfig config, BlockPos origin) {
            this.level = level;
            this.rand = rand;
            this.config = config;
            this.basePos = origin;
        }

        boolean generate() {
            heightLimit = HEIGHT_LIMIT_LIMIT + rand.nextInt(HEIGHT_LIMIT_LIMIT);
            if (!validTreeLocation()) {
                return false;
            }
            generateLeafNodeList();
            generateLeaves();
            generateTrunk();
            generateLeafNodeBases();
            TreeLeafUpdater.run(level, placedLogs, placedLeaves, freshLeaves);
            return true;
        }

        private void generateLeafNodeList() {
            height = (int) (heightLimit * HEIGHT_ATTENUATION);
            if (height >= heightLimit) {
                height = heightLimit - 1;
            }
            int nodesPerLayer = (int) (1.382 + Math.pow(LEAF_DENSITY * heightLimit / 13.0, 2.0));
            if (nodesPerLayer < 1) {
                nodesPerLayer = 1;
            }
            int branchTop = basePos.getY() + height;
            int layer = heightLimit - LEAF_DISTANCE_LIMIT;
            foliageCoords = new ArrayList<>();
            foliageCoords.add(new FoliageNode(basePos.above(layer), branchTop));
            for (; layer >= 0; layer--) {
                float size = layerSize(layer);
                if (size < 0.0F) {
                    continue;
                }
                for (int l = 0; l < nodesPerLayer; l++) {
                    double radius = SCALE_WIDTH * size * (rand.nextFloat() + 0.328);
                    double angle = rand.nextFloat() * 2.0F * Math.PI;
                    double offsetX = radius * Math.sin(angle) + 0.5;
                    double offsetZ = radius * Math.cos(angle) + 0.5;
                    BlockPos nodePos = basePos.offset(Mth.floor(offsetX), layer - 1, Mth.floor(offsetZ));
                    BlockPos nodeTop = nodePos.above(LEAF_DISTANCE_LIMIT);
                    if (checkBlockLine(nodePos, nodeTop) == -1) {
                        int distX = basePos.getX() - nodePos.getX();
                        int distZ = basePos.getZ() - nodePos.getZ();
                        double drop = nodePos.getY() - Math.sqrt(distX * distX + distZ * distZ) * BRANCH_SLOPE;
                        int branchBase = drop > branchTop ? branchTop : (int) drop;
                        BlockPos branchPos = new BlockPos(basePos.getX(), branchBase, basePos.getZ());
                        if (checkBlockLine(branchPos, nodePos) == -1) {
                            foliageCoords.add(new FoliageNode(nodePos, branchBase));
                        }
                    }
                }
            }
        }

        private void crosSection(BlockPos center, float radius) {
            int range = (int) (radius + 0.618);
            for (int dx = -range; dx <= range; dx++) {
                for (int dz = -range; dz <= range; dz++) {
                    if (Math.pow(Math.abs(dx) + 0.5, 2.0) + Math.pow(Math.abs(dz) + 0.5, 2.0) <= radius * radius) {
                        BlockPos pos = center.offset(dx, 0, dz);
                        BlockState state = level.getBlockState(pos);
                        if (state.isAir()) {
                            level.setBlock(pos, config.leaves().defaultBlockState(), PLACE_FLAGS);
                            placedLeaves.add(pos);
                            freshLeaves.add(pos);
                        } else if (state.is(BlockTags.LEAVES)) {
                            if (!state.is(config.leaves())) {
                                level.setBlock(
                                        pos,
                                        TreeLeafUpdater.carryDistance(
                                                config.leaves().defaultBlockState(), state),
                                        PLACE_FLAGS);
                            }
                            placedLeaves.add(pos);
                        }
                    }
                }
            }
        }

        private float layerSize(int layer) {
            if (layer < heightLimit * 0.3F) {
                return -1.0F;
            }
            float half = heightLimit / 2.0F;
            float delta = half - layer;
            float size = Mth.sqrt(half * half - delta * delta);
            if (delta == 0.0F) {
                size = half;
            } else if (Math.abs(delta) >= half) {
                return 0.0F;
            }
            return size * 0.5F;
        }

        private float leafSize(int layer) {
            if (layer < 0 || layer >= LEAF_DISTANCE_LIMIT) {
                return -1.0F;
            }
            return layer != 0 && layer != LEAF_DISTANCE_LIMIT - 1 ? 3.0F : 2.0F;
        }

        private void generateLeafNode(BlockPos pos) {
            for (int i = 0; i < LEAF_DISTANCE_LIMIT; i++) {
                crosSection(pos.above(i), leafSize(i));
            }
        }

        private void limb(BlockPos from, BlockPos to) {
            BlockPos delta = to.offset(-from.getX(), -from.getY(), -from.getZ());
            int steps = getGreatestDistance(delta);
            float stepX = (float) delta.getX() / steps;
            float stepY = (float) delta.getY() / steps;
            float stepZ = (float) delta.getZ() / steps;
            for (int j = 0; j <= steps; j++) {
                BlockPos pos = from.offset(
                        Mth.floor(0.5F + j * stepX), Mth.floor(0.5F + j * stepY), Mth.floor(0.5F + j * stepZ));
                Direction.Axis axis = getLogAxis(from, pos);
                BlockState state = config.log().defaultBlockState();
                if (state.hasProperty(RotatedPillarBlock.AXIS)) {
                    state = state.setValue(RotatedPillarBlock.AXIS, axis);
                }
                level.setBlock(pos, state, PLACE_FLAGS);
                placedLogs.add(pos);
            }
        }

        private int getGreatestDistance(BlockPos delta) {
            int dx = Mth.abs(delta.getX());
            int dy = Mth.abs(delta.getY());
            int dz = Mth.abs(delta.getZ());
            return dz > dx && dz > dy ? dz : Math.max(dy, dx);
        }

        private Direction.Axis getLogAxis(BlockPos from, BlockPos to) {
            Direction.Axis axis = Direction.Axis.Y;
            int dx = Math.abs(to.getX() - from.getX());
            int dz = Math.abs(to.getZ() - from.getZ());
            int max = Math.max(dx, dz);
            if (max > 0) {
                if (dx == max) {
                    axis = Direction.Axis.X;
                } else if (dz == max) {
                    axis = Direction.Axis.Z;
                }
            }
            return axis;
        }

        private void generateLeaves() {
            for (FoliageNode node : foliageCoords) {
                if (leafNodeNeedsBase(node.branchBase() - basePos.getY())) {
                    generateLeafNode(node.pos());
                }
            }
        }

        private boolean leafNodeNeedsBase(int heightAboveBase) {
            return heightAboveBase >= heightLimit * 0.2;
        }

        private void generateTrunk() {
            limb(basePos, basePos.above(height));
        }

        private void generateLeafNodeBases() {
            for (FoliageNode node : foliageCoords) {
                int branchBase = node.branchBase();
                BlockPos branchPos = new BlockPos(basePos.getX(), branchBase, basePos.getZ());
                if (leafNodeNeedsBase(branchBase - basePos.getY())) {
                    limb(branchPos, node.pos());
                }
            }
        }

        private int checkBlockLine(BlockPos from, BlockPos to) {
            BlockPos delta = to.offset(-from.getX(), -from.getY(), -from.getZ());
            int steps = getGreatestDistance(delta);
            if (steps == 0) {
                return -1;
            }
            float stepX = (float) delta.getX() / steps;
            float stepY = (float) delta.getY() / steps;
            float stepZ = (float) delta.getZ() / steps;
            for (int j = 0; j <= steps; j++) {
                BlockPos pos = from.offset(
                        Mth.floor(0.5F + j * stepX), Mth.floor(0.5F + j * stepY), Mth.floor(0.5F + j * stepZ));
                if (!isReplaceable(pos)) {
                    return j;
                }
            }
            return -1;
        }

        private boolean isReplaceable(BlockPos pos) {
            BlockState state = level.getBlockState(pos);
            return state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.canBeReplaced();
        }

        private boolean validTreeLocation() {
            if (!level.getFluidState(basePos).isEmpty()) {
                return false;
            }
            BlockState soil = level.getBlockState(basePos.below());
            if (!soil.is(BlockTags.DIRT) && !soil.is(Blocks.FARMLAND)) {
                return false;
            }
            int clearance = checkBlockLine(basePos, basePos.above(heightLimit - 1));
            if (clearance == -1) {
                return true;
            }
            if (clearance < MIN_CLEARANCE) {
                return false;
            }
            heightLimit = clearance;
            return true;
        }
    }
}
