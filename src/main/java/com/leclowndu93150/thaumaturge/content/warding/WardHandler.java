package com.leclowndu93150.thaumaturge.content.warding;

import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.network.ClientboundWardChunkPayload;
import com.leclowndu93150.thaumaturge.network.ClientboundWardUpdatePayload;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class WardHandler {
    private WardHandler() {}

    public static boolean isWarded(LevelAccessor level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            return find(serverLevel, pos) != null;
        }
        return level.isClientSide() && ClientWardHolder.isWarded(pos);
    }

    public static @Nullable UUID owner(ServerLevel level, BlockPos pos) {
        WardLocation ward = find(level, pos);
        return ward == null ? null : ward.data().owner(ward.pos());
    }

    public static boolean canAccess(ServerLevel level, BlockPos pos, UUID player) {
        WardLocation ward = find(level, pos);
        return ward != null
                && (player.equals(ward.data().owner(ward.pos())) || ward.data().canAccess(ward.pos(), player));
    }

    public static boolean canDelegateIron(ServerLevel level, BlockPos pos, UUID player) {
        WardLocation ward = find(level, pos);
        return ward != null
                && (player.equals(ward.data().owner(ward.pos())) || ward.data().canDelegateIron(ward.pos(), player));
    }

    public static boolean grantAccess(ServerLevel level, BlockPos pos, UUID player, boolean gold) {
        WardLocation ward = find(level, pos);
        LevelChunk chunk = ward == null ? null : LevelSpaces.loadedChunk(level, ward.pos());
        if (ward == null || chunk == null || !ward.data().grantAccess(ward.pos(), player, gold)) {
            return false;
        }
        chunk.setUnsaved(true);
        return true;
    }

    public static boolean canWard(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir()
                && !state.hasBlockEntity()
                && (!state.getCollisionShape(level, pos).isEmpty()
                        || state.is(TTBlocks.WARDED_GLASS.get())
                        || state.is(TTBlocks.ARCANE_DOOR.get())
                        || state.is(TTBlocks.ARCANE_PRESSURE_PLATE.get()))
                && state.getDestroySpeed(level, pos) >= 0.0F;
    }

    public static boolean ward(ServerLevel level, BlockPos pos, UUID owner) {
        LevelChunk chunk = LevelSpaces.loadedChunk(level, pos);
        if (chunk == null || !canWard(level, pos) || isWarded(level, pos)) {
            return false;
        }
        chunk.getData(TTAttachments.WARDS.get()).put(pos, owner);
        chunk.setUnsaved(true);
        sendUpdate(level, chunk, new ClientboundWardUpdatePayload(pos, Optional.of(owner)));
        return true;
    }

    public static boolean unward(ServerLevel level, BlockPos pos, UUID owner) {
        WardLocation ward = find(level, pos);
        LevelChunk chunk = ward == null ? null : LevelSpaces.loadedChunk(level, ward.pos());
        if (ward == null
                || chunk == null
                || !owner.equals(ward.data().owner(ward.pos()))
                || !ward.data().remove(ward.pos())) {
            return false;
        }
        chunk.setUnsaved(true);
        sendUpdate(level, chunk, new ClientboundWardUpdatePayload(ward.pos(), Optional.empty()));
        return true;
    }

    public static void carry(ServerLevel fromLevel, BlockPos from, ServerLevel toLevel, BlockPos to) {
        WardLocation source = find(fromLevel, from);
        LevelChunk target = LevelSpaces.loadedChunk(toLevel, to);
        if (source == null || target == null) {
            return;
        }
        WardAccess ward = source.data().take(source.pos());
        if (ward == null) {
            return;
        }
        LevelChunk sourceChunk = LevelSpaces.loadedChunk(fromLevel, source.pos());
        if (sourceChunk == null) {
            source.data().restore(source.pos(), ward);
            return;
        }
        sourceChunk.setUnsaved(true);
        sendUpdate(fromLevel, sourceChunk, new ClientboundWardUpdatePayload(source.pos(), Optional.empty()));
        target.getData(TTAttachments.WARDS.get()).restore(to, ward);
        target.setUnsaved(true);
        sendUpdate(toLevel, target, new ClientboundWardUpdatePayload(to, Optional.of(ward.owner())));
    }

    public static void syncChunk(ServerPlayer player, LevelChunk chunk) {
        ClientboundWardChunkPayload payload = chunkPayload(chunk);
        if (payload != null) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    public static @Nullable ClientboundWardChunkPayload chunkPayload(LevelChunk chunk) {
        if (!chunk.hasData(TTAttachments.WARDS.get())) {
            return null;
        }
        WardChunkData data = chunk.getData(TTAttachments.WARDS.get());
        return data.isEmpty() ? null : ClientboundWardChunkPayload.of(chunk.getPos(), data.owners());
    }

    public static void prune(LevelChunk chunk) {
        if (!chunk.hasData(TTAttachments.WARDS.get())) {
            return;
        }
        WardChunkData data = chunk.getData(TTAttachments.WARDS.get());
        Map<BlockPos, UUID> owners = data.owners();
        if (owners.keySet().removeIf(pos -> !canWard(chunk, pos))) {
            chunk.setUnsaved(true);
        }
    }

    private static @Nullable WardChunkData existing(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = LevelSpaces.loadedChunk(level, pos);
        if (chunk == null) return null;
        return chunk.hasData(TTAttachments.WARDS.get()) ? chunk.getData(TTAttachments.WARDS.get()) : null;
    }

    private static void sendUpdate(ServerLevel level, LevelChunk chunk, ClientboundWardUpdatePayload payload) {
        PacketDistributor.sendToPlayersTrackingChunk(level, chunk.getPos(), payload);
    }

    private static @Nullable WardLocation find(ServerLevel level, BlockPos pos) {
        WardChunkData data = existing(level, pos);
        return data != null && data.contains(pos) ? new WardLocation(data, pos) : null;
    }

    private record WardLocation(WardChunkData data, BlockPos pos) {}

    public static @Nullable BlockPos partner(BlockState state, BlockPos pos) {
        if (!state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return null;
        }
        return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER
                ? pos.above()
                : pos.below();
    }
}
