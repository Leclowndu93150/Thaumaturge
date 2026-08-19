package com.leclowndu93150.thaumaturge.content.world.tree;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public final class BigTreeFeature extends Feature<BigTreeConfig> {
    private static final byte[] OTHER_COORD_PAIRS = {2, 0, 0, 1, 2, 1};
    private static final double LEAF_DENSITY = 0.9;
    private static final int HEIGHT_LIMIT_LIMIT = 11;
    private static final int LEAF_DISTANCE_LIMIT = 4;
    private static final double SECOND_CANOPY_SCALE_WIDTH = 1.66;
    private static final int SPIDER_WEB_ATTEMPTS = 50;
    private static final int PLACE_FLAGS = 19;

    public BigTreeFeature(Codec<BigTreeConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BigTreeConfig> context) {
        return new Generator(context.level(), context.random(), context.config(), context.origin()).generate();
    }

    private static final class Generator {
        private final WorldGenLevel level;
        private final RandomSource rand;
        private final BigTreeConfig config;
        private final BlockPos origin;
        private final Set<BlockPos> placedLogs = new HashSet<>();
        private final Set<BlockPos> placedLeaves = new HashSet<>();
        private final Set<BlockPos> freshLeaves = new HashSet<>();
        private final int[] basePos = new int[3];
        private int heightLimit;
        private int height;
        private double scaleWidth;
        private int[][] leafNodes;

        Generator(WorldGenLevel level, RandomSource rand, BigTreeConfig config, BlockPos origin) {
            this.level = level;
            this.rand = rand;
            this.config = config;
            this.origin = origin;
            this.scaleWidth = config.scaleWidth();
        }

        boolean generate() {
            basePos[0] = origin.getX();
            basePos[1] = origin.getY();
            basePos[2] = origin.getZ();
            heightLimit = HEIGHT_LIMIT_LIMIT + rand.nextInt(HEIGHT_LIMIT_LIMIT);
            for (int dx = 0; dx < config.trunkSize(); dx++) {
                for (int dz = 0; dz < config.trunkSize(); dz++) {
                    if (!validTreeLocation(dx, dz)) {
                        return false;
                    }
                }
            }
            level.setBlock(origin, Blocks.AIR.defaultBlockState(), PLACE_FLAGS);
            generateLeafNodeList();
            generateLeaves();
            generateLeafNodeBases();
            generateTrunk();
            if (config.doubleCanopy()) {
                scaleWidth = SECOND_CANOPY_SCALE_WIDTH;
                basePos[0] = origin.getX();
                basePos[1] = origin.getY() + height;
                basePos[2] = origin.getZ();
                generateLeafNodeList();
                generateLeaves();
                generateLeafNodeBases();
                generateTrunk();
            }
            if (config.spiderChance() > 0.0F && rand.nextFloat() < config.spiderChance()) {
                generateSpiderNest();
            }
            TreeLeafUpdater.run(level, placedLogs, placedLeaves, freshLeaves);
            return true;
        }

        private void generateSpiderNest() {
            BlockPos spawnerPos = origin.below();
            level.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), PLACE_FLAGS);
            if (level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
                spawner.setEntityId(EntityType.CAVE_SPIDER, rand);
                for (int a = 0; a < SPIDER_WEB_ATTEMPTS; a++) {
                    BlockPos webPos = new BlockPos(
                            origin.getX() - 7 + rand.nextInt(14),
                            origin.getY() + rand.nextInt(10),
                            origin.getZ() - 7 + rand.nextInt(14));
                    if (level.getBlockState(webPos).isAir() && isTouchingTree(webPos)) {
                        level.setBlock(webPos, Blocks.COBWEB.defaultBlockState(), PLACE_FLAGS);
                    }
                }
                BlockPos chestPos = origin.below(2);
                level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), PLACE_FLAGS);
                RandomizableContainer.setBlockEntityLootTable(level, rand, chestPos, BuiltInLootTables.SIMPLE_DUNGEON);
            }
        }

        private boolean isTouchingTree(BlockPos pos) {
            for (Direction direction : Direction.values()) {
                BlockState neighbor = level.getBlockState(pos.relative(direction));
                if (neighbor.is(config.leaves()) || neighbor.is(config.log())) {
                    return true;
                }
            }
            return false;
        }

        private void generateLeafNodeList() {
            height = (int) (heightLimit * config.heightAttenuation());
            if (height >= heightLimit) {
                height = heightLimit - 1;
            }
            int nodesPerLayer = (int) (1.382 + Math.pow(LEAF_DENSITY * heightLimit / 13.0, 2.0));
            if (nodesPerLayer < 1) {
                nodesPerLayer = 1;
            }
            int[][] nodes = new int[nodesPerLayer * heightLimit][4];
            int layerY = basePos[1] + heightLimit - LEAF_DISTANCE_LIMIT;
            int nodeCount = 1;
            int branchTop = basePos[1] + height;
            int layerOffset = layerY - basePos[1];
            nodes[0][0] = basePos[0];
            nodes[0][1] = layerY;
            nodes[0][2] = basePos[2];
            nodes[0][3] = branchTop;
            layerY--;
            while (layerOffset >= 0) {
                float size = layerSize(layerOffset);
                if (size < 0.0F) {
                    layerY--;
                    layerOffset--;
                } else {
                    int attempt = 0;
                    for (double half = 0.5; attempt < nodesPerLayer; attempt++) {
                        double radius = scaleWidth * size * (rand.nextFloat() + 0.328);
                        double angle = rand.nextFloat() * 2.0 * Math.PI;
                        int nodeX = Mth.floor(radius * Math.sin(angle) + basePos[0] + half);
                        int nodeZ = Mth.floor(radius * Math.cos(angle) + basePos[2] + half);
                        int[] nodeBase = {nodeX, layerY, nodeZ};
                        int[] nodeTop = {nodeX, layerY + LEAF_DISTANCE_LIMIT, nodeZ};
                        if (checkBlockLine(nodeBase, nodeTop) == -1) {
                            int[] branchBase = {basePos[0], basePos[1], basePos[2]};
                            double dist = Math.sqrt(Math.pow(Math.abs(basePos[0] - nodeBase[0]), 2.0)
                                    + Math.pow(Math.abs(basePos[2] - nodeBase[2]), 2.0));
                            double drop = dist * config.branchSlope();
                            if (nodeBase[1] - drop > branchTop) {
                                branchBase[1] = branchTop;
                            } else {
                                branchBase[1] = (int) (nodeBase[1] - drop);
                            }
                            if (checkBlockLine(branchBase, nodeBase) == -1) {
                                nodes[nodeCount][0] = nodeX;
                                nodes[nodeCount][1] = layerY;
                                nodes[nodeCount][2] = nodeZ;
                                nodes[nodeCount][3] = branchBase[1];
                                nodeCount++;
                            }
                        }
                    }
                    layerY--;
                    layerOffset--;
                }
            }
            leafNodes = new int[nodeCount][4];
            System.arraycopy(nodes, 0, leafNodes, 0, nodeCount);
        }

        private void genTreeLayer(int x, int y, int z, float radius, byte axis, Block block) {
            int range = (int) (radius + 0.618);
            byte coordA = OTHER_COORD_PAIRS[axis];
            byte coordB = OTHER_COORD_PAIRS[axis + 3];
            int[] center = {x, y, z};
            int[] cursor = {0, 0, 0};
            cursor[axis] = center[axis];
            for (int da = -range; da <= range; da++) {
                cursor[coordA] = center[coordA] + da;
                for (int db = -range; db <= range; db++) {
                    double distSq = Math.pow(Math.abs(da) + 0.5, 2.0) + Math.pow(Math.abs(db) + 0.5, 2.0);
                    if (distSq <= radius * radius) {
                        cursor[coordB] = center[coordB] + db;
                        BlockPos pos = new BlockPos(cursor[0], cursor[1], cursor[2]);
                        BlockState state = level.getBlockState(pos);
                        if (state.isAir()) {
                            level.setBlock(pos, block.defaultBlockState(), PLACE_FLAGS);
                            placedLeaves.add(pos);
                            freshLeaves.add(pos);
                        } else if (state.is(config.leaves())) {
                            placedLeaves.add(pos);
                        }
                    }
                }
            }
        }

        private float layerSize(int layer) {
            if (layer < heightLimit * 0.3) {
                return -1.618F;
            }
            float half = heightLimit / 2.0F;
            float delta = heightLimit / 2.0F - layer;
            float size;
            if (delta == 0.0F) {
                size = half;
            } else if (Math.abs(delta) >= half) {
                size = 0.0F;
            } else {
                size = (float) Math.sqrt(Math.pow(Math.abs(half), 2.0) - Math.pow(Math.abs(delta), 2.0));
            }
            return size * 0.5F;
        }

        private float leafSize(int layer) {
            if (layer < 0 || layer >= LEAF_DISTANCE_LIMIT) {
                return -1.0F;
            }
            return layer != 0 && layer != LEAF_DISTANCE_LIMIT - 1 ? 3.0F : 2.0F;
        }

        private void generateLeafNode(int x, int y, int z) {
            for (int layerY = y; layerY < y + LEAF_DISTANCE_LIMIT; layerY++) {
                genTreeLayer(x, layerY, z, leafSize(layerY - y), (byte) 1, config.leaves());
            }
        }

        private void placeBlockLine(int[] from, int[] to, Block block) {
            int[] delta = {0, 0, 0};
            byte primary = 0;
            for (byte i = 0; i < 3; i++) {
                delta[i] = to[i] - from[i];
                if (Math.abs(delta[i]) > Math.abs(delta[primary])) {
                    primary = i;
                }
            }
            if (delta[primary] == 0) {
                return;
            }
            byte coordA = OTHER_COORD_PAIRS[primary];
            byte coordB = OTHER_COORD_PAIRS[primary + 3];
            byte step = (byte) (delta[primary] > 0 ? 1 : -1);
            double slopeA = (double) delta[coordA] / delta[primary];
            double slopeB = (double) delta[coordB] / delta[primary];
            int[] cursor = {0, 0, 0};
            int progress = 0;
            for (int end = delta[primary] + step; progress != end; progress += step) {
                cursor[primary] = Mth.floor(from[primary] + progress + 0.5);
                cursor[coordA] = Mth.floor(from[coordA] + progress * slopeA + 0.5);
                cursor[coordB] = Mth.floor(from[coordB] + progress * slopeB + 0.5);
                byte axisMeta = 1;
                int distX = Math.abs(cursor[0] - from[0]);
                int distZ = Math.abs(cursor[2] - from[2]);
                int maxDist = Math.max(distX, distZ);
                if (maxDist > 0) {
                    if (distX == maxDist) {
                        axisMeta = 0;
                    } else if (distZ == maxDist) {
                        axisMeta = 2;
                    }
                }
                BlockPos pos = new BlockPos(cursor[0], cursor[1], cursor[2]);
                if (isReplaceable(pos)) {
                    level.setBlock(pos, logState(block, axisMeta), PLACE_FLAGS);
                    placedLogs.add(pos);
                }
            }
        }

        private BlockState logState(Block block, byte axisMeta) {
            BlockState state = block.defaultBlockState();
            if (state.hasProperty(RotatedPillarBlock.AXIS)) {
                Direction.Axis axis =
                        switch (axisMeta) {
                            case 0 -> Direction.Axis.X;
                            case 2 -> Direction.Axis.Z;
                            default -> Direction.Axis.Y;
                        };
                state = state.setValue(RotatedPillarBlock.AXIS, axis);
            }
            return state;
        }

        private boolean isReplaceable(BlockPos pos) {
            BlockState state = level.getBlockState(pos);
            return state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.canBeReplaced();
        }

        private void generateLeaves() {
            for (int[] node : leafNodes) {
                if (leafNodeNeedsBase(node[3] - basePos[1])) {
                    generateLeafNode(node[0], node[1], node[2]);
                }
            }
        }

        private boolean leafNodeNeedsBase(int heightAboveBase) {
            return heightAboveBase >= heightLimit * 0.2;
        }

        private void generateTrunk() {
            int x = basePos[0];
            int yBottom = basePos[1];
            int yTop = basePos[1] + height;
            int z = basePos[2];
            int[] from = {x, yBottom, z};
            int[] to = {x, yTop, z};
            placeBlockLine(from, to, config.log());
            if (config.trunkSize() == 2) {
                from[0]++;
                to[0]++;
                placeBlockLine(from, to, config.log());
                from[2]++;
                to[2]++;
                placeBlockLine(from, to, config.log());
                from[0]--;
                to[0]--;
                placeBlockLine(from, to, config.log());
            }
        }

        private void generateLeafNodeBases() {
            int[] branchBase = {basePos[0], basePos[1], basePos[2]};
            for (int[] node : leafNodes) {
                int[] nodePos = {node[0], node[1], node[2]};
                branchBase[1] = node[3];
                int heightAboveBase = branchBase[1] - basePos[1];
                if (leafNodeNeedsBase(heightAboveBase)) {
                    placeBlockLine(branchBase, nodePos, config.log());
                }
            }
        }

        private int checkBlockLine(int[] from, int[] to) {
            int[] delta = {0, 0, 0};
            byte primary = 0;
            for (byte i = 0; i < 3; i++) {
                delta[i] = to[i] - from[i];
                if (Math.abs(delta[i]) > Math.abs(delta[primary])) {
                    primary = i;
                }
            }
            if (delta[primary] == 0) {
                return -1;
            }
            byte coordA = OTHER_COORD_PAIRS[primary];
            byte coordB = OTHER_COORD_PAIRS[primary + 3];
            byte step = (byte) (delta[primary] > 0 ? 1 : -1);
            double slopeA = (double) delta[coordA] / delta[primary];
            double slopeB = (double) delta[coordB] / delta[primary];
            int[] cursor = {0, 0, 0};
            int progress = 0;
            int end = delta[primary] + step;
            while (progress != end) {
                cursor[primary] = from[primary] + progress;
                cursor[coordA] = Mth.floor(from[coordA] + progress * slopeA);
                cursor[coordB] = Mth.floor(from[coordB] + progress * slopeB);
                BlockState state = level.getBlockState(new BlockPos(cursor[0], cursor[1], cursor[2]));
                if (!state.isAir() && !state.is(config.leaves())) {
                    break;
                }
                progress += step;
            }
            return progress == end ? -1 : Math.abs(progress);
        }

        private boolean validTreeLocation(int offsetX, int offsetZ) {
            int[] from = {basePos[0] + offsetX, basePos[1], basePos[2] + offsetZ};
            int[] to = {basePos[0] + offsetX, basePos[1] + heightLimit - 1, basePos[2] + offsetZ};
            BlockPos trunkPos = new BlockPos(basePos[0] + offsetX, basePos[1], basePos[2] + offsetZ);
            if (!level.getFluidState(trunkPos).isEmpty()) {
                return false;
            }
            BlockPos soilPos = trunkPos.below();
            BlockState soil = level.getBlockState(soilPos);
            if (!soil.is(BlockTags.DIRT) && !soil.is(Blocks.FARMLAND)) {
                return false;
            }
            int clearance = checkBlockLine(from, to);
            if (clearance == -1) {
                return true;
            }
            if (clearance < 6) {
                return false;
            }
            heightLimit = clearance;
            return true;
        }
    }
}
