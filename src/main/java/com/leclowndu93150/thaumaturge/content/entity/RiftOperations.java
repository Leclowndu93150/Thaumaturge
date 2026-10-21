package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.rifts.RiftAccess;
import com.leclowndu93150.thaumaturge.api.rifts.RiftAuthorization;
import com.leclowndu93150.thaumaturge.api.rifts.RiftChange;
import com.leclowndu93150.thaumaturge.api.rifts.RiftMutation;
import com.leclowndu93150.thaumaturge.content.aura.AuraManager;
import com.leclowndu93150.thaumaturge.api.rifts.RiftOperationContext;
import com.leclowndu93150.thaumaturge.api.rifts.RiftOperationEvent;
import com.leclowndu93150.thaumaturge.api.rifts.RiftResult;
import com.leclowndu93150.thaumaturge.api.rifts.RiftSnapshot;
import com.leclowndu93150.thaumaturge.api.rifts.RiftSpec;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public final class RiftOperations implements RiftAccess.Bindings {
    @Override
    public Optional<RiftSnapshot> inspect(ServerLevel level, UUID identity) {
        EntityFluxRift rift = find(level, identity);
        return rift == null ? Optional.empty() : Optional.of(rift.snapshot());
    }

    @Override
    public List<RiftSnapshot> query(ServerLevel level, AABB area, int limit) {
        if (!level.getServer().isSameThread() || limit < 1 || limit > 64 || !finite(area) || area.getXsize() > 64 || area.getYsize() > 64 || area.getZsize() > 64) {
            return List.of();
        }
        List<EntityFluxRift> found = new ArrayList<>(limit);
        level.getEntities(EntityTypeTest.forClass(EntityFluxRift.class), area, rift -> !rift.isRemoved(), found, limit);
        return found.stream().map(EntityFluxRift::snapshot).toList();
    }

    public static @Nullable EntityFluxRift find(ServerLevel level, UUID identity) {
        if (!level.getServer().isSameThread() || identity == null) {
            return null;
        }
        Entity entity = level.getEntity(identity);
        return entity instanceof EntityFluxRift rift && !rift.isRemoved() && level.hasChunkAt(rift.blockPosition()) ? rift : null;
    }

    public static boolean valid(RiftChange change) {
        return change != null && finite(change.position()) && change.size() >= 1 && change.size() <= EntityFluxRift.MAX_RIFT_SIZE && Float.isFinite(change.stability()) && change.stability() >= -100
                && change.stability() <= 100;
    }

    public static AABB bounds(Vec3 position, int size, int seed) {
        List<Vec3> points = new ArrayList<>();
        List<Float> widths = new ArrayList<>();
        RiftOutline.build(size, seed, points, widths);
        return RiftBounds.around(points, position).inflate(0.5);
    }

    public static RiftResult guard(RiftOperationContext context, RiftAuthorization.Action action, AABB source, AABB destination) {
        ServerLevel level = context.level();
        if (!level.getServer().isSameThread()) {
            return RiftResult.UNLOADED;
        }
        if (!finite(source) || !finite(destination) || source.getXsize() > 32 || source.getYsize() > 32 || source.getZsize() > 32 || destination.getXsize() > 32 || destination.getYsize() > 32
                || destination.getZsize() > 32) {
            return RiftResult.INVALID;
        }
        for (AABB volume : List.of(source, destination)) {
            BlockPos minimum = BlockPos.containing(volume.minX, volume.minY, volume.minZ);
            BlockPos maximum = BlockPos.containing(volume.maxX, volume.maxY, volume.maxZ);
            if (!level.isInWorldBounds(minimum) || !level.isInWorldBounds(maximum)) {
                return RiftResult.INVALID;
            }
            if (!level.getWorldBorder().isWithinBounds(minimum) || !level.getWorldBorder().isWithinBounds(maximum)) {
                return RiftResult.DENIED;
            }
        }
        if (!loaded(level, source) || !loaded(level, destination)) {
            return RiftResult.UNLOADED;
        }
        if (!RiftAuthorizations.validate(context, action, source) || !RiftAuthorizations.validate(context, action, destination)) {
            return RiftResult.DENIED;
        }
        ServerPlayer player = RiftAuthorizations.actor(context).map(level.getServer().getPlayerList()::getPlayer).orElse(null);
        if (player != null) {
            if (!player.isAlive() || player.isSpectator() || !player.mayBuild() || player.isRemoved()) {
                return RiftResult.DENIED;
            }
            if (!nativeProtection(level, player, source, false) || !nativeProtection(level, player, destination, action != RiftAuthorization.Action.CAPTURE)) {
                return RiftResult.DENIED;
            }
        }
        if (NeoForge.EVENT_BUS.post(new RiftOperationEvent(context, action, source, destination)).isCanceled()) {
            return RiftResult.DENIED;
        }
        return RiftAuthorizations.validate(context, action, source) && RiftAuthorizations.validate(context, action, destination) ? RiftResult.SUCCESS : RiftResult.DENIED;
    }

    private static boolean nativeProtection(ServerLevel level, ServerPlayer player, AABB volume, boolean placing) {
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(volume.minX, volume.minY, volume.minZ), BlockPos.containing(volume.maxX, volume.maxY, volume.maxZ))) {
            if (!player.mayInteract(level, pos) || CommonHooks.fireBlockBreak(level, player.gameMode.getGameModeForPlayer(), player, pos, level.getBlockState(pos)).isCanceled()) {
                return false;
            }
            if (placing && EventHooks.onBlockPlace(player, BlockSnapshot.create(level.dimension(), level, pos), Direction.UP)) {
                return false;
            }
        }
        return true;
    }

    public static boolean loaded(ServerLevel level, AABB volume) {
        BlockPos minimum = BlockPos.containing(volume.minX, volume.minY, volume.minZ);
        BlockPos maximum = BlockPos.containing(volume.maxX, volume.maxY, volume.maxZ);
        if (!level.isInWorldBounds(minimum) || !level.isInWorldBounds(maximum) || !level.getWorldBorder().isWithinBounds(minimum) || !level.getWorldBorder().isWithinBounds(maximum)) {
            return false;
        }
        for (int chunkX = minimum.getX() >> 4; chunkX <= maximum.getX() >> 4; chunkX++) {
            for (int chunkZ = minimum.getZ() >> 4; chunkZ <= maximum.getZ() >> 4; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public RiftMutation prepareUpdate(RiftOperationContext context, RiftSnapshot expected, RiftChange change) {
        if (Transaction.getLifecycle() != Transaction.Lifecycle.NONE) {
            return new RejectedMutation(RiftResult.BUSY);
        }
        RiftResult result = checkUpdate(context, expected, change, false);
        return result == RiftResult.SUCCESS ? new Mutation(context, expected, change) : new RejectedMutation(result);
    }

    private static RiftResult checkUpdate(RiftOperationContext context, RiftSnapshot expected, RiftChange change, boolean reserved) {
        if (!valid(change) || expected == null) {
            return RiftResult.INVALID;
        }
        if (!expected.dimension().equals(context.level().dimension())) {
            return RiftResult.STALE;
        }
        EntityFluxRift rift = find(context.level(), expected.identity());
        if (rift == null) {
            return RiftResult.UNLOADED;
        }
        RiftSnapshot before = rift.snapshot();
        if (!valid(RiftChange.of(before))) {
            return RiftResult.UNSUPPORTED;
        }
        if (before.revision() != expected.revision()) {
            return RiftResult.STALE;
        }
        if (before.collapsing()) {
            return RiftResult.COLLAPSING;
        }
        if (before.busy() && !reserved) {
            return RiftResult.BUSY;
        }
        boolean moved = !before.position().equals(change.position());
        boolean changed = before.size() != change.size() || before.seed() != change.seed() || before.stability() != change.stability() || change.collapse();
        RiftAuthorization.Action action = moved ? RiftAuthorization.Action.MOVE : RiftAuthorization.Action.MUTATE;
        AABB source = bounds(before.position(), before.size(), before.seed());
        AABB destination = bounds(change.position(), change.size(), change.seed());
        RiftResult gate = guard(context, action, source, destination);
        if (gate != RiftResult.SUCCESS) {
            return gate;
        }
        if (moved && changed) {
            gate = guard(context, RiftAuthorization.Action.MUTATE, source, destination);
            if (gate != RiftResult.SUCCESS) {
                return gate;
            }
        }
        if (find(context.level(), expected.identity()) != rift || !rift.snapshot().equals(before)) {
            return RiftResult.STALE;
        }
        return RiftResult.SUCCESS;
    }

    private record RejectedMutation(RiftResult result) implements RiftMutation {
        @Override
        public TransactionContext transaction() {
            throw new IllegalStateException("The rift mutation was rejected");
        }

        @Override
        public RiftResult commit() {
            return result;
        }

        @Override
        public void close() {}
    }

    private static final class Mutation implements RiftMutation {
        private final RiftOperationContext context;
        private final RiftSnapshot expected;
        private final RiftChange change;
        private final EntityFluxRift rift;
        private final Transaction transaction;
        private boolean closed;
        private RiftResult result = RiftResult.SUCCESS;

        private Mutation(RiftOperationContext context, RiftSnapshot expected, RiftChange change) {
            this.context = context;
            this.expected = expected;
            this.change = change;
            this.rift = find(context.level(), expected.identity());
            this.transaction = Transaction.openRoot();
            rift.stageChange(change, transaction);
        }

        @Override
        public RiftResult result() {
            return result;
        }

        @Override
        public TransactionContext transaction() {
            if (closed) {
                throw new IllegalStateException("The rift mutation is closed");
            }
            return transaction;
        }

        @Override
        public RiftResult commit() {
            if (closed) {
                return RiftResult.STALE;
            }
            try {
                result = find(context.level(), expected.identity()) == rift ? checkUpdate(context, expected, change, true) : RiftResult.STALE;
                if (result == RiftResult.SUCCESS) {
                    transaction.commit();
                }
                return result;
            } finally {
                close();
            }
        }

        @Override
        public void close() {
            if (!closed) {
                transaction.close();
                closed = true;
            }
        }
    }

    @Override
    public RiftResult simulateCreation(RiftOperationContext context, RiftSpec spec) {
        if (spec == null || spec.source() == null || !valid(new RiftChange(spec.position(), spec.size(), spec.seed(), spec.stability(), false)) || spec.size() < 5) {
            return RiftResult.INVALID;
        }
        AABB volume = bounds(spec.position(), spec.size(), spec.seed());
        RiftResult gate = guard(context, RiftAuthorization.Action.CREATE, volume, volume);
        if (gate != RiftResult.SUCCESS) {
            return gate;
        }
        if (!context.level().getEntitiesOfClass(EntityFluxRift.class, volume.inflate(1)).isEmpty()) {
            return RiftResult.BUSY;
        }
        if (spec.source() == RiftSpec.Source.CREATIVE) {
            return context.player() != null && context.player().hasInfiniteMaterials() ? RiftResult.SUCCESS : RiftResult.DENIED;
        }
        return AuraHelper.drainFlux(context.level(), BlockPos.containing(spec.position()), spec.size(), true) >= spec.size() ? RiftResult.SUCCESS : RiftResult.INSUFFICIENT_PAYMENT;
    }

    @Override
    public RiftResult tryCreate(RiftOperationContext context, RiftSpec spec) {
        if (Transaction.getLifecycle() != Transaction.Lifecycle.NONE) {
            return RiftResult.BUSY;
        }
        RiftResult gate = simulateCreation(context, spec);
        if (gate != RiftResult.SUCCESS) {
            return gate;
        }
        ServerLevel level = context.level();
        EntityFluxRift rift = TTEntities.FLUX_RIFT.get().create(level, EntitySpawnReason.TRIGGERED);
        if (rift == null) {
            return RiftResult.UNSUPPORTED;
        }
        BlockPos origin = BlockPos.containing(spec.position());
        boolean committed = false;
        try (Transaction transaction = Transaction.openRoot()) {
            if (spec.source() == RiftSpec.Source.FLUX && AuraManager.withdrawFlux(level, origin, spec.size(), transaction) < spec.size()) {
                return RiftResult.INSUFFICIENT_PAYMENT;
            }
            rift.restoreSnapshot(new RiftSnapshot(rift.getUUID(), level.dimension(), 0, spec.position(), spec.size(), spec.seed(), spec.stability(), false, false));
            rift.suspendTransfer();
            if (!level.addFreshEntity(rift)) {
                return RiftResult.DENIED;
            }
            AABB volume = bounds(spec.position(), spec.size(), spec.seed());
            RiftResult committedGate = guard(context, RiftAuthorization.Action.CREATE, volume, volume);
            if (committedGate != RiftResult.SUCCESS || find(level, rift.getUUID()) != rift || !rift.position().equals(spec.position()) || rift.currentSize() != spec.size()
                    || rift.shapeSeed() != spec.seed() || rift.stabilityValue() != spec.stability() || rift.isCollapsing()) {
                return committedGate == RiftResult.SUCCESS ? RiftResult.STALE : committedGate;
            }
            transaction.commit();
            rift.resumeTransfer();
            committed = true;
            return RiftResult.SUCCESS;
        } finally {
            if (!committed) {
                rift.discard();
            }
        }
    }

    private static boolean finite(Vec3 position) {
        return position != null && Double.isFinite(position.x) && Double.isFinite(position.y) && Double.isFinite(position.z);
    }

    private static boolean finite(AABB area) {
        return area != null && Double.isFinite(area.minX) && Double.isFinite(area.minY) && Double.isFinite(area.minZ) && Double.isFinite(area.maxX) && Double.isFinite(area.maxY)
                && Double.isFinite(area.maxZ);
    }
}
