package com.leclowndu93150.thaumaturge.content.world.space;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.Event;

public final class BlocksMovedEvent extends Event {
    private final ServerLevel from;
    private final ServerLevel to;
    private final Map<BlockPos, BlockPos> moves;
    private final Rotation rotation;

    public BlocksMovedEvent(ServerLevel from, ServerLevel to, Map<BlockPos, BlockPos> moves, Rotation rotation) {
        this.from = from;
        this.to = to;
        this.moves = Map.copyOf(moves);
        this.rotation = rotation;
    }

    public ServerLevel from() {
        return from;
    }

    public ServerLevel to() {
        return to;
    }

    public Map<BlockPos, BlockPos> moves() {
        return moves;
    }

    public Rotation rotation() {
        return rotation;
    }

    public boolean withinLevel() {
        return from == to;
    }
}
