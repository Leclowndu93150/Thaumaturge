package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.api.aura.AuraBaseline;
import com.leclowndu93150.thaumaturge.api.aura.AuraBaseline.Context;
import com.leclowndu93150.thaumaturge.api.aura.AuraBaseline.Quote;
import com.leclowndu93150.thaumaturge.api.aura.AuraBaseline.Result;
import com.leclowndu93150.thaumaturge.api.aura.AuraBaselineEvent;
import com.leclowndu93150.thaumaturge.api.aura.IAuraBaselineMachine;
import com.leclowndu93150.thaumaturge.network.ClientboundAuraSnapshotPayload;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class AuraBaselineBindings implements AuraBaseline.Bindings {
    private static final long MAX_DURATION = 7 * 24000L;
    private static final int MAX_GRANTS = 4096;

    @Override
    public Optional<Quote> quote(Context context) {
        return authorize(context).isEmpty() ? Optional.empty() : sample(context);
    }

    private static Optional<Quote> sample(Context context) {
        AuraData data = AuraManager.chunkAt(context.level(), context.chunk());
        if (data == null || !data.isInitialized() || data.baselinePending()) {
            return Optional.empty();
        }
        return AuraGenHandler.loadedBiomeRevision(context.level(), context.chunk()).map(
                revision -> new Quote(context.level().dimension(), context.chunk(), data.baselineRevision(), revision, data.getBase(), AuraGenHandler.generateBase(context.level(), context.chunk())));
    }

    @Override
    public Result recalculate(Context context, Quote expected, long sequence, TransactionContext transaction) {
        if (sequence <= 0 || expected == null || !expected.dimension().equals(context.level().dimension()) || !expected.chunk().equals(context.chunk())) {
            return Result.INVALID;
        }
        Optional<UUID> authority = authorize(context);
        if (authority.isEmpty()) {
            return Result.DENIED;
        }
        AuraData data = AuraManager.chunkAt(context.level(), context.chunk());
        if (data == null || !data.isInitialized()) {
            return Result.UNAVAILABLE;
        }
        if (data.baselinePending()) {
            return Result.BUSY;
        }
        long previous = data.lastBaselineOperation(authority.get());
        if (sequence == previous) {
            return Result.ALREADY_APPLIED;
        }
        if (sequence < previous) {
            return Result.STALE;
        }
        Optional<Quote> fresh = sample(context);
        if (fresh.isEmpty()) {
            return Result.UNAVAILABLE;
        }
        if (!fresh.get().equals(expected)) {
            return Result.STALE;
        }
        if (!data.canRecordBaseline(authority.get()) || data.baselineRevision() == Long.MAX_VALUE) {
            return Result.LIMIT_REACHED;
        }
        data.recalculateBase(expected.proposedBase(), authority.get(), sequence, transaction);
        new RootCommitJournal(() -> {
            AuraManager.markChunkDirty(context.level(), context.chunk());
            PacketDistributor.sendToPlayersTrackingChunk(context.level(), context.chunk(),
                    new ClientboundAuraSnapshotPayload(context.chunk().x(), context.chunk().z(), data.getBase(), data.getVis(), data.getFlux()));
        }).updateSnapshots(transaction);
        return Result.APPLIED;
    }

    @Override
    public Optional<UUID> delegate(ServerPlayer owner, BlockPos machine, ChunkPos target, long durationTicks) {
        ServerLevel level = owner.level();
        Context context = Context.player(owner, target);
        if (durationTicks <= 0 || durationTicks > MAX_DURATION || !level.getServer().isSameThread() || !level.hasChunkAt(machine)
                || owner.distanceToSqr(machine.getX() + 0.5, machine.getY() + 0.5, machine.getZ() + 0.5) > 64 || !level.mayInteract(owner, machine) || authorize(context).isEmpty()) {
            return Optional.empty();
        }
        if (!(level.getBlockEntity(machine) instanceof IAuraBaselineMachine device) || !owner.getUUID().equals(device.auraMachineOwner()) || device.auraMachineIdentity() == null) {
            return Optional.empty();
        }
        if (Math.abs((long) target.x() - (machine.getX() >> 4)) > 8 || Math.abs((long) target.z() - (machine.getZ() >> 4)) > 8) {
            return Optional.empty();
        }
        AuraBaselineDelegations data = AuraBaselineDelegations.get(level.getServer());
        long now = level.getServer().overworld().getGameTime();
        if (data.grants.values().removeIf(grant -> grant.expires() <= now)) {
            data.setDirty();
        }
        if (data.grants.size() >= MAX_GRANTS) {
            return Optional.empty();
        }
        UUID token = UUID.randomUUID();
        data.grants.put(token, new AuraBaselineDelegations.Grant(token, owner.getUUID(), device.auraMachineIdentity(), level.dimension().identifier(), machine, ChunkPos.pack(target.x(), target.z()),
                now + durationTicks));
        data.setDirty();
        return Optional.of(token);
    }

    @Override
    public boolean revoke(ServerPlayer owner, UUID delegation) {
        if (!owner.level().getServer().isSameThread()) {
            return false;
        }
        AuraBaselineDelegations data = AuraBaselineDelegations.get(owner.level().getServer());
        AuraBaselineDelegations.Grant grant = data.grants.get(delegation);
        if (grant == null || !grant.owner().equals(owner.getUUID())) {
            return false;
        }
        data.grants.remove(delegation);
        data.setDirty();
        return true;
    }

    private static Optional<UUID> authorize(Context context) {
        Optional<Authority> before = resolveAuthority(context);
        if (before.isEmpty() || NeoForge.EVENT_BUS.post(new AuraBaselineEvent(context, before.get().owner(), before.get().offline())).isCanceled()) {
            return Optional.empty();
        }
        return resolveAuthority(context).filter(before.get()::equals).map(Authority::identity);
    }

    private record Authority(UUID owner, UUID identity, boolean offline) {
    }

    private static Optional<Authority> resolveAuthority(Context context) {
        ServerLevel level = context.level();
        ChunkPos chunk = context.chunk();
        if (!level.getServer().isSameThread() || level.getChunkSource().getChunkNow(chunk.x(), chunk.z()) == null) {
            return Optional.empty();
        }
        ServerPlayer player = context.player().orElse(null);
        UUID owner;
        UUID authority;
        if (player != null) {
            owner = player.getUUID();
            authority = owner;
        } else {
            AuraBaselineDelegations.Grant grant = AuraBaselineDelegations.get(level.getServer()).grants.get(context.delegation().orElseThrow());
            if (grant == null || grant.expires() <= level.getServer().overworld().getGameTime() || grant.chunk() != ChunkPos.pack(chunk.x(), chunk.z())
                    || !grant.dimension().equals(level.dimension().identifier()) || !level.hasChunkAt(grant.position())
                    || !(level.getBlockEntity(grant.position()) instanceof IAuraBaselineMachine machine) || level.getBlockEntity(grant.position()).isRemoved()
                    || !grant.machine().equals(machine.auraMachineIdentity()) || !grant.owner().equals(machine.auraMachineOwner())) {
                return Optional.empty();
            }
            owner = grant.owner();
            authority = UUID.nameUUIDFromBytes(("aura_machine:" + owner + ":" + grant.machine()).getBytes(StandardCharsets.UTF_8));
            player = level.getServer().getPlayerList().getPlayer(owner);
            if (player != null && (!player.isAlive() || player.isSpectator())) {
                return Optional.empty();
            }
        }
        if (player != null && (player.level() != level || !player.isAlive() || player.isRemoved() || player.isSpectator() || !player.mayBuild())) {
            return Optional.empty();
        }
        if (!permittedChunk(level, chunk, player)) {
            return Optional.empty();
        }
        return Optional.of(new Authority(owner, authority, player == null));
    }

    private static boolean permittedChunk(ServerLevel level, ChunkPos chunk, @Nullable ServerPlayer player) {
        for (int offsetX = 0; offsetX < 16; offsetX++) {
            for (int offsetZ = 0; offsetZ < 16; offsetZ++) {
                BlockPos pos = new BlockPos(chunk.getMinBlockX() + offsetX, 50, chunk.getMinBlockZ() + offsetZ);
                if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos) || player != null && !level.mayInteract(player, pos)) {
                    return false;
                }
            }
        }
        return true;
    }
}
