package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEssentiaTransport;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class AdvancedAlchemicalFurnaceStructure {
    private static final int RADIUS = 1;
    private static final int HEIGHT = 2;

    private AdvancedAlchemicalFurnaceStructure() {}

    public static boolean isLoaded(Level level, BlockPos controller) {
        return level.hasChunksAt(
                controller.getX() - RADIUS,
                controller.getZ() - RADIUS,
                controller.getX() + RADIUS,
                controller.getZ() + RADIUS);
    }

    public static boolean isFormed(Level level, BlockPos controller, BlockPos.MutableBlockPos cursor) {
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }
                boolean corner = x != 0 && z != 0;
                cursor.setWithOffset(controller, x, 0, z);
                if (!level.getBlockState(cursor)
                        .is(
                                corner
                                        ? TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER
                                        : TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE)) {
                    return false;
                }
                cursor.move(Direction.UP);
                if (!level.getBlockState(cursor)
                        .is(
                                corner
                                        ? TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER
                                        : TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isPart(BlockState state) {
        return state.is(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER)
                || state.is(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER)
                || state.is(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER)
                || state.is(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE);
    }

    public static void restore(LevelAccessor level, BlockPos controller) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    cursor.setWithOffset(controller, x, y, z);
                    BlockState original = originalOf(level.getBlockState(cursor));
                    if (original != null) {
                        level.setBlock(cursor.immutable(), original, Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    public static void disassembleAround(LevelAccessor level, BlockPos part) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = 1 - HEIGHT; y <= 0; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    cursor.setWithOffset(part, x, y, z);
                    if (level.getBlockState(cursor).is(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE)) {
                        level.setBlock(
                                cursor.immutable(), TTBlocks.SMELTER_BASIC.get().defaultBlockState(), Block.UPDATE_ALL);
                        return;
                    }
                }
            }
        }
    }

    public static void refreshNozzles(Level level, BlockPos controller) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos nozzle = controller.relative(direction);
            level.invalidateCapabilities(nozzle);
            BlockEssentiaTransport.refreshConnectionsAround(level, nozzle);
        }
    }

    public static @Nullable IEssentiaTransport nozzle(Level level, BlockPos nozzle, @Nullable Direction side) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockEntity(nozzle.relative(direction))
                    instanceof BlockEntityAdvancedAlchemicalFurnace furnace) {
                AdvancedFurnaceNozzle port = furnace.nozzle(direction.getOpposite());
                return side == null || port.isConnectable(side) ? port : null;
            }
        }
        return null;
    }

    private static @Nullable BlockState originalOf(BlockState state) {
        if (state.is(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER)) {
            return TTBlocks.ALEMBIC.get().defaultBlockState();
        }
        if (state.is(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER)) {
            return TTBlocks.ALCHEMICAL_CONSTRUCT.get().defaultBlockState();
        }
        if (state.is(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER)
                || state.is(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE)) {
            return TTBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get().defaultBlockState();
        }
        return null;
    }
}
