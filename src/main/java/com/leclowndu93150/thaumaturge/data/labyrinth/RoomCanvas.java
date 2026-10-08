package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeGeometry;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomSocket;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.RoomVoxels;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.SocketProfile;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

final class RoomCanvas extends AbstractTemplateCanvas implements RoomVoxels {
    static final int F = SocketProfile.FLOOR;
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private final Long2ObjectMap<LabyrinthMarker> markers = new Long2ObjectLinkedOpenHashMap<>();
    private final Predicate<BlockState> passable;

    RoomCanvas(int width, int depth, Predicate<BlockState> passable) {
        super(width * MazeGeometry.CELL, SocketProfile.HEIGHT, depth * MazeGeometry.CELL);
        this.passable = passable;
    }

    void set(int x, int y, int z, BlockState state) {
        if (inside(x, y, z)) {
            put(x, y, z, state);
            markers.remove(BlockPos.asLong(x, y, z));
        }
    }

    void carve(RoomShape shape) {
        fill(shape, AIR);
    }

    void fill(RoomShape shape, BlockState state) {
        for (int x = 0; x < sizeX(); x++) {
            for (int y = 0; y < sizeY(); y++) {
                for (int z = 0; z < sizeZ(); z++) {
                    if (shape.contains(x, y, z)) {
                        set(x, y, z, state);
                    }
                }
            }
        }
    }

    void socket(RoomSocket socket, int length) {
        Direction inward = socket.side().getOpposite();
        for (int step = 0; step < length; step++) {
            for (int u = 0; u < MazeGeometry.CELL; u++) {
                for (int y = 0; y < sizeY(); y++) {
                    if (SocketProfile.open(u, y)) {
                        BlockPos pos = SocketProfile.planePos(socket.cellX(), socket.cellZ(), socket.side(), u, y);
                        set(pos.getX() + inward.getStepX() * step, y, pos.getZ() + inward.getStepZ() * step, AIR);
                    }
                }
            }
        }
    }

    void run(Direction.Axis axis, int cell, int from, int to) {
        int base = cell * MazeGeometry.CELL;
        for (int along = Math.min(from, to); along <= Math.max(from, to); along++) {
            for (int u = 0; u < MazeGeometry.CELL; u++) {
                for (int y = 0; y < sizeY(); y++) {
                    if (SocketProfile.open(u, y)) {
                        if (axis == Direction.Axis.Z) {
                            set(base + u, y, along, AIR);
                        } else {
                            set(along, y, base + u, AIR);
                        }
                    }
                }
            }
        }
    }

    void marker(int x, int y, int z, LabyrinthMarker marker) {
        if (inside(x, y, z)) {
            put(x, y, z, MARKER);
            markers.put(BlockPos.asLong(x, y, z), marker);
        }
    }

    @Override
    public Optional<String> metadata(int x, int y, int z) {
        LabyrinthMarker marker = markers.get(BlockPos.asLong(x, y, z));
        return marker == null
                ? Optional.empty()
                : Optional.of(LabyrinthMarker.CODEC
                        .encodeStart(JsonOps.INSTANCE, marker)
                        .getOrThrow()
                        .toString());
    }

    void skin(SkinPalette palette) {
        BlockState[] wrapped = new BlockState[sizeX() * sizeY() * sizeZ()];
        for (int x = 0; x < sizeX(); x++) {
            for (int y = 0; y < sizeY(); y++) {
                for (int z = 0; z < sizeZ(); z++) {
                    if (get(x, y, z) == null) {
                        wrapped[RoomVoxels.index(x, y, z, sizeY(), sizeZ())] = skinFor(palette, x, y, z);
                    }
                }
            }
        }
        for (int x = 0; x < sizeX(); x++) {
            for (int y = 0; y < sizeY(); y++) {
                for (int z = 0; z < sizeZ(); z++) {
                    BlockState state = wrapped[RoomVoxels.index(x, y, z, sizeY(), sizeZ())];
                    if (state != null) {
                        put(x, y, z, state);
                    }
                }
            }
        }
    }

    private BlockState skinFor(SkinPalette palette, int x, int y, int z) {
        if (isOpen(x, y + 1, z)) {
            return palette.floor();
        }
        if (isOpen(x, y - 1, z)) {
            return palette.ceiling();
        }
        if (isOpen(x + 1, y, z) || isOpen(x - 1, y, z) || isOpen(x, y, z + 1) || isOpen(x, y, z - 1)) {
            return palette.wall();
        }
        return null;
    }

    private boolean isOpen(int x, int y, int z) {
        return inside(x, y, z) && kind(x, y, z).open();
    }

    @Override
    public Kind kind(int x, int y, int z) {
        BlockState state = get(x, y, z);
        if (state == null) {
            return Kind.UNSPECIFIED;
        }
        if (state.isAir()) {
            return Kind.AIR;
        }
        if (state.is(Blocks.STRUCTURE_BLOCK)) {
            return Kind.MARKER;
        }
        return passable.test(state) || state.getBlock() instanceof StairBlock ? Kind.PASSABLE : Kind.SOLID;
    }
}
