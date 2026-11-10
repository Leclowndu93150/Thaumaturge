package com.leclowndu93150.thaumaturge.content.warding;

import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
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
            WardChunkData data = existing(serverLevel, pos);
            return data != null && data.contains(pos);
        }
        return level.isClientSide() && ClientWardHolder.isWarded(pos);
    }

    public static @Nullable UUID owner(ServerLevel level, BlockPos pos) {
        WardChunkData data = existing(level, pos);
        return data == null ? null : data.owner(pos);
    }

    public static boolean canAccess(ServerLevel level, BlockPos pos, UUID player) {
        WardChunkData data = existing(level, pos);
        return player.equals(owner(level, pos)) || (data != null && data.canAccess(pos, player));
    }

    public static boolean canDelegateIron(ServerLevel level, BlockPos pos, UUID player) {
        WardChunkData data = existing(level, pos);
        return player.equals(owner(level, pos)) || (data != null && data.canDelegateIron(pos, player));
    }

    public static boolean grantAccess(ServerLevel level, BlockPos pos, UUID player, boolean gold) {
        WardChunkData data = existing(level, pos);
        if (data == null || !data.contains(pos) || !data.grantAccess(pos, player, gold)) {
            return false;
        }
        level.getChunkAt(pos).setUnsaved(true);
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
        if (!LoadedChunks.contains(level, pos) || !canWard(level, pos) || isWarded(level, pos)) {
            return false;
        }
        LevelChunk chunk = level.getChunkAt(pos);
        chunk.getData(TTAttachments.WARDS.get()).put(pos, owner);
        chunk.setUnsaved(true);
        PacketDistributor.sendToPlayersTrackingChunk(
                level, chunk.getPos(), new ClientboundWardUpdatePayload(pos, Optional.of(owner)));
        return true;
    }

    public static boolean unward(ServerLevel level, BlockPos pos, UUID owner) {
        if (!LoadedChunks.contains(level, pos)) {
            return false;
        }
        WardChunkData data = existing(level, pos);
        if (data == null || !owner.equals(data.owner(pos)) || !data.remove(pos)) {
            return false;
        }
        LevelChunk chunk = level.getChunkAt(pos);
        chunk.setUnsaved(true);
        PacketDistributor.sendToPlayersTrackingChunk(
                level, chunk.getPos(), new ClientboundWardUpdatePayload(pos, Optional.empty()));
        return true;
    }

    public static void carry(ServerLevel fromLevel, BlockPos from, ServerLevel toLevel, BlockPos to) {
        WardChunkData data = existing(fromLevel, from);
        if (data == null || !data.contains(from) || !LoadedChunks.contains(toLevel, to)) {
            return;
        }
        WardAccess ward = data.take(from);
        LevelChunk source = fromLevel.getChunkAt(from);
        source.setUnsaved(true);
        PacketDistributor.sendToPlayersTrackingChunk(
                fromLevel, source.getPos(), new ClientboundWardUpdatePayload(from, Optional.empty()));
        LevelChunk target = toLevel.getChunkAt(to);
        target.getData(TTAttachments.WARDS.get()).restore(to, ward);
        target.setUnsaved(true);
        PacketDistributor.sendToPlayersTrackingChunk(
                toLevel, target.getPos(), new ClientboundWardUpdatePayload(to, Optional.of(ward.owner())));
    }

    public static void syncChunk(ServerPlayer player, LevelChunk chunk) {
        if (!chunk.hasData(TTAttachments.WARDS.get())) {
            return;
        }
        WardChunkData data = chunk.getData(TTAttachments.WARDS.get());
        if (data.isEmpty()) {
            return;
        }
        PacketDistributor.sendToPlayer(player, ClientboundWardChunkPayload.of(chunk.getPos(), data.owners()));
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
        if (!LoadedChunks.contains(level, pos)) {
            return null;
        }
        LevelChunk chunk = level.getChunkAt(pos);
        return chunk.hasData(TTAttachments.WARDS.get()) ? chunk.getData(TTAttachments.WARDS.get()) : null;
    }

    public static @Nullable BlockPos partner(BlockState state, BlockPos pos) {
        if (!state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return null;
        }
        return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER
                ? pos.above()
                : pos.below();
    }
}
