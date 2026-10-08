package com.leclowndu93150.thaumaturge.client.model.connected;

import java.util.Arrays;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class ConnectedStairsBlockStateModel extends AbstractConnectedModel {
    private static final Direction[] FACES = Direction.values();
    private static final int VOXELS = 8;
    private static final int NEIGHBOURHOOD = 27;
    private static final int UNKNOWN = -1;
    private static final int NO_CELL = -1;

    private final StairCellQuads[] quads;
    private final @Nullable TagKey<Block> group;

    public ConnectedStairsBlockStateModel(
            StairCellQuads[] quads,
            TextureAtlasSprite particle,
            @Nullable TagKey<Block> group,
            ItemTransforms transforms,
            RenderType renderType) {
        super(particle, true, transforms, renderType);
        this.quads = quads;
        this.group = group;
    }

    @Override
    protected ConnectedQuads collect(BlockGetter level, BlockPos pos, BlockState state) {
        Neighbourhood hood = new Neighbourhood(level, pos, state, group);
        ConnectedQuads.Builder builder = new ConnectedQuads.Builder();
        int[] cellFrames = new int[StairCellQuads.DEPTHS * StairCellQuads.CELLS];
        int[] cellNotches = new int[StairCellQuads.DEPTHS * StairCellQuads.CELLS];
        for (Direction face : FACES) {
            Arrays.fill(cellFrames, NO_CELL);
            collectCells(hood, face, cellFrames, cellNotches);
            emit(quads[face.ordinal()], cellFrames, cellNotches, builder);
        }
        return builder.build();
    }

    private static void collectCells(Neighbourhood hood, Direction face, int[] cellFrames, int[] cellNotches) {
        Direction up = FaceConnections.textureUp(face);
        Direction right = FaceConnections.textureRight(face);
        for (int voxel = 0; voxel < VOXELS; voxel++) {
            if ((hood.own & 1 << voxel) == 0) {
                continue;
            }
            int x = voxel & 1;
            int z = voxel >> 1 & 1;
            int y = voxel >> 2 & 1;
            if (hood.filled(x + face.getStepX(), y + face.getStepY(), z + face.getStepZ())) {
                continue;
            }
            int depth = along(face, x, y, z) == 1 ? 0 : 1;
            int cell = along(right, x, y, z) | (1 - along(up, x, y, z)) << 1;
            int slot = depth * StairCellQuads.CELLS + cell;
            boolean top = hood.continues(x, y, z, up, null, face);
            boolean bottom = hood.continues(x, y, z, up.getOpposite(), null, face);
            boolean left = hood.continues(x, y, z, right.getOpposite(), null, face);
            boolean rightEdge = hood.continues(x, y, z, right, null, face);
            cellFrames[slot] = (top ? 0 : StairCellQuads.FRAME_TOP)
                    | (bottom ? 0 : StairCellQuads.FRAME_BOTTOM)
                    | (left ? 0 : StairCellQuads.FRAME_LEFT)
                    | (rightEdge ? 0 : StairCellQuads.FRAME_RIGHT);
            int notches = 0;
            if (top && left && !hood.continues(x, y, z, up, right.getOpposite(), face)) {
                notches |= StairCellQuads.NOTCH_TOP_LEFT;
            }
            if (top && rightEdge && !hood.continues(x, y, z, up, right, face)) {
                notches |= StairCellQuads.NOTCH_TOP_RIGHT;
            }
            if (bottom && left && !hood.continues(x, y, z, up.getOpposite(), right.getOpposite(), face)) {
                notches |= StairCellQuads.NOTCH_BOTTOM_LEFT;
            }
            if (bottom && rightEdge && !hood.continues(x, y, z, up.getOpposite(), right, face)) {
                notches |= StairCellQuads.NOTCH_BOTTOM_RIGHT;
            }
            cellNotches[slot] = notches;
        }
    }

    private static void emit(
            StairCellQuads faceQuads, int[] cellFrames, int[] cellNotches, ConnectedQuads.Builder builder) {
        for (int depth = 0; depth < StairCellQuads.DEPTHS; depth++) {
            int base = depth * StairCellQuads.CELLS;
            boolean whole = true;
            for (int cell = 0; cell < StairCellQuads.CELLS && whole; cell++) {
                whole = cellFrames[base + cell] == 0 && cellNotches[base + cell] == 0;
            }
            if (whole) {
                faceQuads.addFace(depth, builder);
                continue;
            }
            for (int cell = 0; cell < StairCellQuads.CELLS; cell++) {
                if (cellFrames[base + cell] != NO_CELL) {
                    faceQuads.addCell(depth, cell, cellFrames[base + cell], cellNotches[base + cell], builder);
                }
            }
        }
    }

    private static int along(Direction direction, int x, int y, int z) {
        int value =
                switch (direction.getAxis()) {
                    case X -> x;
                    case Y -> y;
                    case Z -> z;
                };
        return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? value : 1 - value;
    }

    private static final class Neighbourhood {
        private final BlockGetter level;
        private final BlockPos pos;
        private final BlockState state;
        private final @Nullable TagKey<Block> group;
        private final boolean connects;
        private final int own;
        private final int[] occupancy = new int[NEIGHBOURHOOD];
        private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        private Neighbourhood(BlockGetter level, BlockPos pos, BlockState state, @Nullable TagKey<Block> group) {
            this.level = level;
            this.pos = pos;
            this.state = state;
            this.group = group;
            this.connects = StairVoxels.isStairs(state);
            this.own = StairVoxels.mask(state);
            Arrays.fill(occupancy, UNKNOWN);
        }

        private boolean continues(int x, int y, int z, Direction edge, @Nullable Direction diagonal, Direction face) {
            int nx = x + edge.getStepX() + (diagonal == null ? 0 : diagonal.getStepX());
            int ny = y + edge.getStepY() + (diagonal == null ? 0 : diagonal.getStepY());
            int nz = z + edge.getStepZ() + (diagonal == null ? 0 : diagonal.getStepZ());
            return filled(nx, ny, nz) && !filled(nx + face.getStepX(), ny + face.getStepY(), nz + face.getStepZ());
        }

        private boolean filled(int x, int y, int z) {
            return (occupancy(x >> 1, y >> 1, z >> 1) & StairVoxels.bit(x & 1, y & 1, z & 1)) != 0;
        }

        private int occupancy(int bx, int by, int bz) {
            if (bx == 0 && by == 0 && bz == 0) {
                return own;
            }
            int index = bx + 1 + (by + 1) * 3 + (bz + 1) * 9;
            if (occupancy[index] == UNKNOWN) {
                occupancy[index] =
                        connects ? neighbourOccupancy(level.getBlockState(cursor.setWithOffset(pos, bx, by, bz))) : 0;
            }
            return occupancy[index];
        }

        private int neighbourOccupancy(BlockState other) {
            boolean joins = group != null ? other.is(group) : other.is(state.getBlock());
            if (!joins) {
                return 0;
            }
            return StairVoxels.isStairs(other) ? StairVoxels.mask(other) : StairVoxels.FULL;
        }
    }
}
