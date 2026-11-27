package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

final class AspectReadoutPlacement {
    private static final float JAR_NODE_TOP_LOWERING = 1.0F / 3.0F;

    record Placement(@Nullable Direction side, @Nullable BlockPos diagonal) {}

    private AspectReadoutPlacement() {}

    static @Nullable Placement find(
            Level level, BlockPos pos, Direction preferred, boolean preferTop, Vec3 viewerWorldPosition) {
        if (preferTop && LoadedChunks.contains(level, pos.above()) && canHost(level.getBlockState(pos.above()))) {
            return new Placement(Direction.UP, null);
        }
        Vec3 towardViewer = towardViewer(level, pos, viewerWorldPosition);
        Direction side = visibleSide(level, pos, preferred, towardViewer);
        if (side != null) {
            return new Placement(side, null);
        }
        BlockPos diagonal = frontDiagonal(
                pos,
                towardViewer,
                candidate -> LoadedChunks.contains(level, candidate) && canHost(level.getBlockState(candidate)));
        return diagonal == null ? null : new Placement(null, diagonal);
    }

    static boolean canHost(BlockState state) {
        return state.isAir() || state.is(Tags.Blocks.GLASS_BLOCKS) || state.is(Tags.Blocks.GLASS_PANES);
    }

    static float topReadoutVerticalAdjustment(BlockEntity blockEntity, @Nullable Direction side) {
        return blockEntity instanceof BlockEntityJarNode && side == Direction.UP ? -JAR_NODE_TOP_LOWERING : 0.0F;
    }

    static boolean isViewerFacing(Direction side, Vec3 towardViewer) {
        return side.getAxis().isHorizontal()
                && towardViewer.x * side.getStepX() + towardViewer.z * side.getStepZ() > 1.0E-6;
    }

    private static @Nullable Direction visibleSide(Level level, BlockPos pos, Direction preferred, Vec3 towardViewer) {
        Direction side = preferred.getAxis().isHorizontal()
                ? LevelSpaces.directionToLocal(level, pos, preferred)
                : Direction.NORTH;
        Direction bestSide = null;
        double bestAlignment = 0.0;
        for (int i = 0; i < 4; i++) {
            BlockPos neighborPos = pos.relative(side);
            double alignment = towardViewer.x * side.getStepX() + towardViewer.z * side.getStepZ();
            if (alignment > bestAlignment && LoadedChunks.contains(level, neighborPos)) {
                BlockState neighbor = level.getBlockState(neighborPos);
                if (canHost(neighbor) && isViewerFacing(side, towardViewer)) {
                    bestSide = side;
                    bestAlignment = alignment;
                }
            }
            side = side.getClockWise();
        }
        return bestSide;
    }

    static @Nullable BlockPos frontDiagonal(BlockPos origin, Vec3 towardViewer, Predicate<BlockPos> canHost) {
        double length = Math.sqrt(towardViewer.x * towardViewer.x + towardViewer.z * towardViewer.z);
        if (length < 1.0E-6) {
            return null;
        }

        BlockPos best = null;
        double bestAlignment = 0.0;
        for (int stepX = -1; stepX <= 1; stepX += 2) {
            for (int stepZ = -1; stepZ <= 1; stepZ += 2) {
                double alignment = (towardViewer.x * stepX + towardViewer.z * stepZ) / (length * Math.sqrt(2.0));
                BlockPos candidate = origin.offset(stepX, 0, stepZ);
                if (alignment > bestAlignment && canHost.test(candidate)) {
                    best = candidate;
                    bestAlignment = alignment;
                }
            }
        }
        return best;
    }

    static @Nullable BlockPos frontDiagonal(Level level, BlockPos pos, Vec3 viewerWorldPosition) {
        Vec3 viewerLocal = towardViewer(level, pos, viewerWorldPosition);
        return frontDiagonal(
                pos,
                viewerLocal,
                candidate -> LoadedChunks.contains(level, candidate) && canHost(level.getBlockState(candidate)));
    }

    private static Vec3 towardViewer(Level level, BlockPos pos, Vec3 viewerWorldPosition) {
        return LevelSpaces.frameOf(level, pos).toLocal(viewerWorldPosition).subtract(Vec3.atCenterOf(pos));
    }

    static boolean viewerAbove(Level level, BlockPos pos, Vec3 viewerWorldPosition) {
        return LevelSpaces.frameOf(level, pos).toLocal(viewerWorldPosition).y > pos.getY() + 1.0;
    }
}
