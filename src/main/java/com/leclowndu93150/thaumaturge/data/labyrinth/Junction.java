package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.SocketProfile;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Direction;

enum Junction {
    STRAIGHT(canvas -> canvas.run(Direction.Axis.Z, 0, 0, SocketProfile.LAST), Direction.NORTH, Direction.SOUTH),
    BEND(
            canvas -> {
                canvas.run(Direction.Axis.Z, 0, 0, SocketProfile.MAX_U);
                canvas.run(Direction.Axis.X, 0, SocketProfile.MIN_U, SocketProfile.LAST);
            },
            Direction.NORTH,
            Direction.EAST),
    TEE(
            canvas -> {
                canvas.run(Direction.Axis.X, 0, 0, SocketProfile.LAST);
                canvas.run(Direction.Axis.Z, 0, 0, SocketProfile.MAX_U);
            },
            Direction.NORTH,
            Direction.EAST,
            Direction.WEST),
    CROSS(
            canvas -> {
                canvas.run(Direction.Axis.Z, 0, 0, SocketProfile.LAST);
                canvas.run(Direction.Axis.X, 0, 0, SocketProfile.LAST);
            },
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST);

    private final Consumer<RoomCanvas> runs;
    private final List<Direction> sides;

    Junction(Consumer<RoomCanvas> runs, Direction... sides) {
        this.runs = runs;
        this.sides = List.of(sides);
    }

    List<Direction> sides() {
        return sides;
    }

    void carve(RoomCanvas canvas) {
        runs.accept(canvas);
    }
}
