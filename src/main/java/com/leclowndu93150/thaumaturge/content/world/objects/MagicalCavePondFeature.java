package com.leclowndu93150.thaumaturge.content.world.objects;

import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class MagicalCavePondFeature extends Feature<NoneFeatureConfiguration> {
    private static final int POND_CHANCE = 10;
    private static final int LARGE_POND_CHANCE = 4;
    private static final int SMALL_RADIUS = 1;
    private static final int LARGE_RADIUS = 2;
    private static final int RADIUS_SLACK = 1;
    private static final int GRID_SIZE = LARGE_RADIUS * 2 + 1;
    private static final int CHUNK_MASK = 15;
    private static final int PLACE_FLAGS = 2;
    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    public MagicalCavePondFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        RandomSource random = context.random();
        if (random.nextInt(POND_CHANCE) != 0) {
            return false;
        }

        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockPos surface = origin.below();
        if (!level.getBlockState(origin).isAir()
                || !level.getBlockState(surface).is(TCBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE)) {
            return false;
        }

        int radius = random.nextInt(LARGE_POND_CHANCE) == 0 ? LARGE_RADIUS : SMALL_RADIUS;
        boolean[] cells = pondCells(level, surface, radius);
        if (!hasAny(cells) || !bordersHold(level, surface, cells)) {
            return false;
        }

        BlockPos.MutableBlockPos cellSurface = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos waterPos = new BlockPos.MutableBlockPos();
        for (int dx = -LARGE_RADIUS; dx <= LARGE_RADIUS; dx++) {
            for (int dz = -LARGE_RADIUS; dz <= LARGE_RADIUS; dz++) {
                if (!cells[index(dx, dz)]) {
                    continue;
                }
                cellSurface.setWithOffset(surface, dx, 0, dz);
                waterPos.set(cellSurface).move(Direction.DOWN);
                level.setBlock(cellSurface, Blocks.AIR.defaultBlockState(), PLACE_FLAGS);
                level.scheduleTick(cellSurface, Blocks.AIR, 0);
                markAboveForPostProcessing(level, cellSurface);
                level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), PLACE_FLAGS);
            }
        }
        return true;
    }

    private static boolean[] pondCells(WorldGenLevel level, BlockPos surface, int radius) {
        boolean[] cells = new boolean[GRID_SIZE * GRID_SIZE];
        int localX = surface.getX() & CHUNK_MASK;
        int localZ = surface.getZ() & CHUNK_MASK;
        BlockPos.MutableBlockPos cellSurface = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos waterPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos support = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
        for (int dx = Math.max(-radius, -localX); dx <= Math.min(radius, CHUNK_MASK - localX); dx++) {
            for (int dz = Math.max(-radius, -localZ); dz <= Math.min(radius, CHUNK_MASK - localZ); dz++) {
                if (dx * dx + dz * dz > radius * radius + RADIUS_SLACK) {
                    continue;
                }
                cellSurface.setWithOffset(surface, dx, 0, dz);
                waterPos.set(cellSurface).move(Direction.DOWN);
                support.set(waterPos).move(Direction.DOWN);
                above.set(cellSurface).move(Direction.UP);
                cells[index(dx, dz)] = level.getBlockState(cellSurface).is(TCBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE)
                        && level.getBlockState(above).isAir()
                        && level.getBlockState(waterPos).is(TCBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE)
                        && level.getBlockState(support).isFaceSturdy(level, support, Direction.UP);
            }
        }
        return cells;
    }

    private static boolean bordersHold(WorldGenLevel level, BlockPos surface, boolean[] cells) {
        BlockPos.MutableBlockPos edge = new BlockPos.MutableBlockPos();
        for (int dx = -LARGE_RADIUS; dx <= LARGE_RADIUS; dx++) {
            for (int dz = -LARGE_RADIUS; dz <= LARGE_RADIUS; dz++) {
                if (!cells[index(dx, dz)]) {
                    continue;
                }
                for (Direction direction : HORIZONTAL) {
                    int nx = dx + direction.getStepX();
                    int nz = dz + direction.getStepZ();
                    if (inGrid(nx, nz) && cells[index(nx, nz)]) {
                        continue;
                    }
                    edge.setWithOffset(surface, nx, 0, nz);
                    if (!level.getFluidState(edge).isEmpty()) {
                        return false;
                    }
                    edge.move(Direction.DOWN);
                    BlockState waterEdge = level.getBlockState(edge);
                    if (!waterEdge.isCollisionShapeFullBlock(level, edge) && !waterEdge.is(Blocks.WATER)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean hasAny(boolean[] cells) {
        for (boolean cell : cells) {
            if (cell) {
                return true;
            }
        }
        return false;
    }

    private static boolean inGrid(int dx, int dz) {
        return Math.abs(dx) <= LARGE_RADIUS && Math.abs(dz) <= LARGE_RADIUS;
    }

    private static int index(int dx, int dz) {
        return (dx + LARGE_RADIUS) * GRID_SIZE + dz + LARGE_RADIUS;
    }
}
