package com.leclowndu93150.thaumaturge.content.world.space;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public interface LevelSpaceProvider {
    record TrackedBlock(BlockPos position, BlockPos worldPosition) {}

    SpaceFrame frameOf(Level level, Position pos);

    SpaceFrame frameOf(Level level, Vec3i pos);

    SpaceFrame trackedFrame(Entity entity);

    boolean isPlotChunk(Level level, ChunkPos pos);

    List<SpaceFrame> framesTouching(Level level, AABB worldBox);

    @Nullable
    LevelChunk loadedChunk(ServerLevel level, BlockPos pos);

    default void rememberBlock(ServerLevel level, UUID id, BlockPos pos) {}

    default @Nullable BlockPos rememberedBlockPosition(ServerLevel level, UUID id) {
        return null;
    }

    default @Nullable TrackedBlock trackedBlock(ServerLevel level, UUID id, boolean loadSubLevel) {
        return null;
    }

    default void forgetBlock(ServerLevel level, UUID id) {}

    default void sendToTrackingPlayers(ServerLevel level, BlockPos pos, CustomPacketPayload payload) {
        LevelChunk chunk = loadedChunk(level, pos);
        if (chunk != null) {
            PacketDistributor.sendToPlayersTrackingChunk(level, chunk.getPos(), payload);
        }
    }
}
