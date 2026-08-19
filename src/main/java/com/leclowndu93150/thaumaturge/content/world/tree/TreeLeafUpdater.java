package com.leclowndu93150.thaumaturge.content.world.tree;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public final class TreeLeafUpdater {
    private static final int MAX_LEAF_DISTANCE = 7;
    private static final int PLACE_FLAGS = 19;

    private TreeLeafUpdater() {}

    public static void run(WorldGenLevel level, Set<BlockPos> logs, Set<BlockPos> leaves, Set<BlockPos> freshLeaves) {
        if (logs.isEmpty() && leaves.isEmpty()) {
            return;
        }
        BoundingBox.encapsulatingPositions(Iterables.concat(logs, leaves)).ifPresent(bounds -> {
            DiscreteVoxelShape shape = computeDistances(level, bounds, logs);
            pruneUnreachableLeaves(level, freshLeaves);
            StructureTemplate.updateShapeAtEdge(level, 3, shape, bounds.minX(), bounds.minY(), bounds.minZ());
        });
    }

    public static BlockState carryDistance(BlockState replacement, BlockState previous) {
        OptionalInt distance = LeavesBlock.getOptionalDistanceAt(previous);
        if (distance.isPresent() && replacement.hasProperty(BlockStateProperties.DISTANCE)) {
            return replacement.setValue(BlockStateProperties.DISTANCE, distance.getAsInt());
        }
        return replacement;
    }

    private static void pruneUnreachableLeaves(WorldGenLevel level, Set<BlockPos> freshLeaves) {
        for (BlockPos pos : freshLeaves) {
            BlockState state = level.getBlockState(pos);
            OptionalInt distance = LeavesBlock.getOptionalDistanceAt(state);
            if (distance.isPresent() && distance.getAsInt() == MAX_LEAF_DISTANCE) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), PLACE_FLAGS);
            }
        }
    }

    private static DiscreteVoxelShape computeDistances(WorldGenLevel level, BoundingBox bounds, Set<BlockPos> logs) {
        DiscreteVoxelShape shape = new BitSetDiscreteVoxelShape(bounds.getXSpan(), bounds.getYSpan(), bounds.getZSpan());
        List<Set<BlockPos>> toCheck = new ArrayList<>(MAX_LEAF_DISTANCE);
        for (int i = 0; i < MAX_LEAF_DISTANCE; i++) {
            toCheck.add(new HashSet<>());
        }
        toCheck.get(0).addAll(logs);
        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
        int distance = 0;
        while (distance < MAX_LEAF_DISTANCE) {
            Set<BlockPos> queue = toCheck.get(distance);
            if (queue.isEmpty()) {
                distance++;
                continue;
            }
            Iterator<BlockPos> iterator = queue.iterator();
            BlockPos pos = iterator.next();
            iterator.remove();
            if (!bounds.isInside(pos)) {
                continue;
            }
            if (distance != 0) {
                BlockState state = level.getBlockState(pos);
                level.setBlock(pos, state.setValue(BlockStateProperties.DISTANCE, distance), PLACE_FLAGS);
            }
            shape.fill(pos.getX() - bounds.minX(), pos.getY() - bounds.minY(), pos.getZ() - bounds.minZ());
            for (Direction direction : Direction.values()) {
                neighborPos.setWithOffset(pos, direction);
                if (!bounds.isInside(neighborPos)) {
                    continue;
                }
                int shapeX = neighborPos.getX() - bounds.minX();
                int shapeY = neighborPos.getY() - bounds.minY();
                int shapeZ = neighborPos.getZ() - bounds.minZ();
                if (shape.isFull(shapeX, shapeY, shapeZ)) {
                    continue;
                }
                OptionalInt current = LeavesBlock.getOptionalDistanceAt(level.getBlockState(neighborPos));
                if (current.isEmpty()) {
                    continue;
                }
                int newDistance = Math.min(current.getAsInt(), distance + 1);
                if (newDistance < MAX_LEAF_DISTANCE) {
                    toCheck.get(newDistance).add(neighborPos.immutable());
                    distance = Math.min(distance, newDistance);
                }
            }
        }
        return shape;
    }
}
