package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.rifts.CapturedRift;
import com.leclowndu93150.thaumaturge.api.rifts.IRiftCaptureItem;
import com.leclowndu93150.thaumaturge.api.rifts.RiftAuthorization.Action;
import com.leclowndu93150.thaumaturge.api.rifts.RiftCapturePolicy;
import com.leclowndu93150.thaumaturge.api.rifts.RiftContents;
import com.leclowndu93150.thaumaturge.api.rifts.RiftOperationContext;
import com.leclowndu93150.thaumaturge.api.rifts.RiftResult;
import com.leclowndu93150.thaumaturge.api.rifts.RiftSnapshot;
import com.leclowndu93150.thaumaturge.api.rifts.RiftTransfers;
import com.leclowndu93150.thaumaturge.api.wands.WandPayment;
import com.leclowndu93150.thaumaturge.api.wands.WandSlot;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public final class RiftTransferOperations implements RiftTransfers.Bindings {
    private record Prepared(RiftOperationContext context, UUID identity, long revision, Container inventory, int slot, ItemStack stack, ItemStack fingerprint, RiftCapturePolicy policy, long tick,
            int wandSlot, @Nullable WandSlot wand, WandPayment.@Nullable Quote payment) implements RiftTransfers.Quote {
    }

    @Override
    public Optional<CapturedRift> captured(ItemStack stack) {
        return Optional.ofNullable(stack.get(TTDataComponents.CAPTURED_RIFT));
    }

    @Override
    public Optional<RiftTransfers.Quote> prepareCapture(RiftOperationContext context, UUID identity, long revision, int jarSlot, int wandSlot) {
        EntityFluxRift rift = RiftOperations.find(context.level(), identity);
        if (rift == null || rift.snapshot().revision() != revision || rift.snapshot().busy() || rift.isCollapsing() || rift.currentSize() < 1 || rift.currentSize() > 100) {
            return Optional.empty();
        }
        AABB bounds = RiftOperations.bounds(rift.position(), rift.currentSize(), rift.shapeSeed());
        if (RiftOperations.guard(context, Action.CAPTURE, bounds, bounds) != RiftResult.SUCCESS) {
            return Optional.empty();
        }
        Container inventory = inventory(context);
        ItemStack stack = vessel(inventory, jarSlot);
        if (stack.isEmpty() || stack.has(TTDataComponents.CAPTURED_RIFT)) {
            return Optional.empty();
        }
        RiftCapturePolicy policy = ((IRiftCaptureItem) stack.getItem()).capturePolicy();
        if (rift.currentSize() > policy.maximumSize() || !research(context, policy)) {
            return Optional.empty();
        }
        WandSlot wand = null;
        WandPayment.Quote payment = null;
        if (policy.costs().values().stream().anyMatch(amount -> amount > 0)) {
            if (context.player() == null || jarSlot >= 36 || wandSlot == jarSlot || wandSlot < 0 || wandSlot >= 36) {
                return Optional.empty();
            }
            wand = WandSlot.playerInventory(context.player(), wandSlot);
            payment = WandPayment.quote(context.player(), wand, policy.costs(), WandPayment.CostKind.RAW).orElse(null);
            if (payment == null || WandVisHelper.partsOf(wand.stack()).rod().visStorage() != null) {
                return Optional.empty();
            }
        }
        return Optional.of(new Prepared(context, identity, revision, inventory, jarSlot, stack, stack.copy(), policy, context.level().getGameTime(), wandSlot, wand, payment));
    }

    @Override
    public RiftResult tryCapture(RiftOperationContext context, RiftTransfers.Quote quote) {
        if (!(quote instanceof Prepared prepared) || !prepared.context().equals(context) || context.level().getGameTime() != prepared.tick()) {
            return RiftResult.STALE;
        }
        EntityFluxRift rift = RiftOperations.find(context.level(), prepared.identity());
        if (rift == null || !reconcile(rift) || rift.snapshot().revision() != prepared.revision()) {
            return RiftResult.STALE;
        }
        if (rift.isCollapsing()) {
            return RiftResult.COLLAPSING;
        }
        if (rift.snapshot().busy()) {
            return RiftResult.BUSY;
        }
        if (rift.currentSize() < 1 || rift.currentSize() > 100) {
            return RiftResult.INVALID;
        }
        AABB bounds = RiftOperations.bounds(rift.position(), rift.currentSize(), rift.shapeSeed());
        RiftResult gate = RiftOperations.guard(context, Action.CAPTURE, bounds, bounds);
        if (gate != RiftResult.SUCCESS) {
            return gate;
        }
        if (!current(prepared) || !research(context, prepared.policy()) || rift.currentSize() > prepared.policy().maximumSize()) {
            return RiftResult.STALE;
        }
        UUID owner = RiftAuthorizations.actor(context).orElse(null);
        if (owner == null) {
            return RiftResult.DENIED;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            ItemStack beforeWand = prepared.wand() == null ? ItemStack.EMPTY : prepared.wand().stack().copy();
            if (prepared.payment() != null && (context.player() == null || !WandPayment.tryPay(context.player(), prepared.wand(), prepared.payment(), transaction))) {
                return RiftResult.INSUFFICIENT_PAYMENT;
            }
            if (!current(prepared) || RiftOperations.find(context.level(), prepared.identity()) != rift || rift.snapshot().revision() != prepared.revision()) {
                return RiftResult.STALE;
            }
            CapturedRift captured = CapturedRift.create(rift.getUUID(), new RiftContents(rift.currentSize(), rift.shapeSeed(), rift.stabilityValue(), rift.snapshot().revision()));
            ItemStack filled = prepared.stack().copy();
            filled.set(TTDataComponents.CAPTURED_RIFT, captured);
            if (!prepared.inventory().canPlaceItem(prepared.slot(), filled)) {
                return RiftResult.INVALID;
            }
            Optional<RiftTransferJournal.Checkpoint> checkpoint = prepared.payment() == null
                    ? Optional.empty()
                    : Optional.of(
                            new RiftTransferJournal.Checkpoint(prepared.slot(), prepared.wandSlot(), prepared.fingerprint().copy(), beforeWand, prepared.wand().stack().copy(), rift.transferToken()));
            RiftTransferJournal.Entry entry = new RiftTransferJournal.Entry(captured, owner, new RiftTransferJournal.Location(context.level().dimension(), rift.position()), Optional.empty(), true,
                    checkpoint);
            Optional<RiftTransferJournal.Entry> previous;
            try {
                previous = RiftTransferJournal.read(context.level(), rift.getUUID());
                if (checkpoint.isPresent()) {
                    context.level().save(null, true, false);
                }
                RiftTransferJournal.write(context.level(), entry);
            } catch (IOException exception) {
                return RiftResult.BUSY;
            }
            try {
                prepared.inventory().setItem(prepared.slot(), filled);
            } catch (RuntimeException exception) {
                try {
                    RiftTransferJournal.restore(context.level(), rift.getUUID(), previous);
                    prepared.inventory().setItem(prepared.slot(), prepared.fingerprint().copy());
                } catch (IOException | RuntimeException recoveryFailure) {
                    rift.suspendTransfer();
                    Thaumaturge.LOGGER.error("Rift capture inventory rollback needs recovery", recoveryFailure);
                }
                return RiftResult.BUSY;
            }
            rift.suspendTransfer();
            if (checkpoint.isPresent()) {
                try {
                    RiftPlayerCheckpoint.save(context.player(), captured.token());
                    if (resolveCheckpoint(context.level(), entry).isEmpty()) {
                        rift.resumeTransfer();
                        return RiftResult.BUSY;
                    }
                } catch (IOException | RuntimeException exception) {
                    try {
                        if (resolveCheckpoint(context.level(), entry).isEmpty()) {
                            rift.resumeTransfer();
                            return RiftResult.BUSY;
                        }
                    } catch (IOException unresolved) {
                        transaction.commit();
                        Thaumaturge.LOGGER.error("Rift capture payment checkpoint needs recovery", unresolved);
                        return RiftResult.BUSY;
                    }
                }
            }
            rift.discard();
            transaction.commit();
            prepared.inventory().setChanged();
            return RiftResult.SUCCESS;
        }
    }

    @Override
    public RiftResult tryRelease(RiftOperationContext context, int jarSlot, Vec3 destination) {
        if (destination == null || !Double.isFinite(destination.x) || !Double.isFinite(destination.y) || !Double.isFinite(destination.z)) {
            return RiftResult.INVALID;
        }
        if (!context.level().getServer().isSameThread()) {
            return RiftResult.DENIED;
        }
        Container inventory = inventory(context);
        ItemStack stack = vessel(inventory, jarSlot);
        CapturedRift captured = stack.get(TTDataComponents.CAPTURED_RIFT);
        if (captured == null) {
            return RiftResult.INVALID;
        }
        ItemStack fingerprint = stack.copy();
        Optional<RiftContents> parsed = captured.contents();
        if (parsed.isEmpty()) {
            return RiftResult.UNSUPPORTED;
        }
        RiftContents contents = parsed.get();
        RiftCapturePolicy policy = ((IRiftCaptureItem) stack.getItem()).capturePolicy();
        AABB bounds = RiftOperations.bounds(destination, contents.size(), contents.seed());
        RiftResult gate = RiftOperations.guard(context, Action.RELEASE, bounds, bounds);
        if (gate != RiftResult.SUCCESS) {
            return gate;
        }
        if (contents.size() > policy.maximumSize() || !research(context, policy)) {
            return RiftResult.DENIED;
        }
        if (inventory(context) != inventory || inventory.getItem(jarSlot) != stack || !ItemStack.matches(stack, fingerprint) || !policy.equals(((IRiftCaptureItem) stack.getItem()).capturePolicy())) {
            return RiftResult.STALE;
        }
        ItemStack empty = stack.copy();
        empty.remove(TTDataComponents.CAPTURED_RIFT);
        if (!inventory.canPlaceItem(jarSlot, empty) || existing(context.level(), captured.identity()) != null) {
            return RiftResult.BUSY;
        }
        try {
            Optional<RiftTransferJournal.Entry> found = RiftTransferJournal.read(context.level(), captured.identity());
            if (found.isPresent() && found.get().checkpoint().isPresent()) {
                found = resolveCheckpoint(context.level(), found.get());
            }
            if (found.isEmpty() || found.get().target().isPresent() || !found.get().captured().equals(captured)) {
                return RiftResult.STALE;
            }
            RiftTransferJournal.Entry entry = found.get();
            RiftTransferJournal.Entry releasing = new RiftTransferJournal.Entry(entry.captured(), entry.owner(), entry.source(),
                    Optional.of(new RiftTransferJournal.Location(context.level().dimension(), destination)), false);
            RiftTransferJournal.write(context.level(), releasing);
            EntityFluxRift released = spawn(context.level(), captured, contents, destination);
            if (released == null) {
                RiftTransferJournal.write(context.level(), entry);
                return RiftResult.BUSY;
            }
            RiftResult revalidated = RiftOperations.guard(context, Action.RELEASE, bounds, bounds);
            if (revalidated != RiftResult.SUCCESS || inventory(context) != inventory || inventory.getItem(jarSlot) != stack || !ItemStack.matches(stack, fingerprint)
                    || !policy.equals(((IRiftCaptureItem) stack.getItem()).capturePolicy()) || !research(context, policy) || released.isRemoved() || !released.getUUID().equals(captured.identity())
                    || !released.position().equals(destination) || released.currentSize() != contents.size() || released.shapeSeed() != contents.seed()
                    || released.stabilityValue() != contents.stability() || released.isCollapsing()) {
                released.discard();
                RiftTransferJournal.write(context.level(), entry);
                return revalidated == RiftResult.SUCCESS ? RiftResult.STALE : revalidated;
            }
            try {
                inventory.setItem(jarSlot, empty);
            } catch (RuntimeException exception) {
                released.discard();
                RiftTransferJournal.write(context.level(), entry);
                if (ItemStack.matches(inventory.getItem(jarSlot), empty)) {
                    inventory.setItem(jarSlot, fingerprint.copy());
                }
                return RiftResult.BUSY;
            }
            inventory.setChanged();
            try {
                settle(context.level(), releasing, released);
            } catch (IOException exception) {
                Thaumaturge.LOGGER.error("Rift release committed but its checkpoint needs recovery", exception);
            }
            return RiftResult.SUCCESS;
        } catch (IOException exception) {
            return RiftResult.BUSY;
        }
    }

    @Override
    public RiftResult recover(RiftOperationContext context, UUID identity, int jarSlot) {
        if (!context.level().getServer().isSameThread()) {
            return RiftResult.DENIED;
        }
        try {
            Optional<RiftTransferJournal.Entry> found = RiftTransferJournal.read(context.level(), identity);
            if (found.isEmpty()) {
                return RiftResult.STALE;
            }
            RiftTransferJournal.Entry entry = found.get();
            Optional<RiftContents> parsed = entry.captured().contents();
            if (parsed.isEmpty()) {
                return RiftResult.UNSUPPORTED;
            }
            RiftContents contents = parsed.get();
            RiftTransferJournal.Location location = entry.target().orElse(entry.source());
            if (!location.dimension().equals(context.level().dimension())) {
                return RiftResult.INVALID;
            }
            AABB bounds = RiftOperations.bounds(location.position(), contents.size(), contents.seed());
            RiftResult gate = RiftOperations.guard(context, entry.target().isPresent() ? Action.RELEASE : Action.CAPTURE, bounds, bounds);
            if (gate != RiftResult.SUCCESS || !RiftAuthorizations.actor(context).filter(entry.owner()::equals).isPresent()) {
                return RiftResult.DENIED;
            }
            if (entry.checkpoint().isPresent()) {
                Optional<RiftTransferJournal.Entry> resolved = resolveCheckpoint(context.level(), entry);
                if (resolved.isEmpty()) {
                    return RiftResult.STALE;
                }
                entry = resolved.get();
                EntityFluxRift source = existing(context.level(), identity);
                if (source != null && !reconcile(source)) {
                    source.suspendTransfer();
                }
            }
            if (entry.target().isPresent()) {
                if (entry.settled()) {
                    return RiftResult.STALE;
                }
                EntityFluxRift rift = existing(context.level(), identity);
                if (rift == null) {
                    rift = spawn(context.level(), entry.captured(), contents, location.position());
                }
                if (rift == null || !rift.transferToken().filter(entry.captured().token()::equals).isPresent()) {
                    return RiftResult.BUSY;
                }
                settle(context.level(), entry, rift);
                return RiftResult.SUCCESS;
            }
            Container inventory = inventory(context);
            ItemStack stack = vessel(inventory, jarSlot);
            if (stack.isEmpty() || stack.has(TTDataComponents.CAPTURED_RIFT)) {
                return RiftResult.INVALID;
            }
            RiftCapturePolicy policy = ((IRiftCaptureItem) stack.getItem()).capturePolicy();
            if (contents.size() > policy.maximumSize() || !research(context, policy)) {
                return RiftResult.DENIED;
            }
            ItemStack filled = stack.copy();
            filled.set(TTDataComponents.CAPTURED_RIFT, entry.captured());
            if (!inventory.canPlaceItem(jarSlot, filled)) {
                return RiftResult.INVALID;
            }
            inventory.setItem(jarSlot, filled);
            inventory.setChanged();
            return RiftResult.SUCCESS;
        } catch (IOException exception) {
            return RiftResult.BUSY;
        }
    }

    public static boolean reconcile(EntityFluxRift rift) {
        if (!(rift.level() instanceof ServerLevel level)) {
            return true;
        }
        try {
            Optional<RiftTransferJournal.Entry> found = RiftTransferJournal.read(level, rift.getUUID());
            if (found.isEmpty()) {
                return true;
            }
            RiftTransferJournal.Entry entry = found.get();
            if (entry.checkpoint().isPresent()) {
                Optional<RiftTransferJournal.Entry> resolved = resolveCheckpoint(level, entry);
                if (resolved.isEmpty()) {
                    rift.resumeTransfer();
                    return true;
                }
                entry = resolved.get();
            }
            if (entry.target().isEmpty() || !rift.transferToken().filter(entry.captured().token()::equals).isPresent()) {
                rift.discard();
                return false;
            }
            if (!entry.settled()) {
                if (!rift.hasPersistedTransferToken()) {
                    return false;
                }
                RiftTransferJournal.write(level, new RiftTransferJournal.Entry(entry.captured(), entry.owner(), entry.source(), entry.target(), true));
            }
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    private static @Nullable EntityFluxRift spawn(ServerLevel level, CapturedRift captured, RiftContents contents, Vec3 destination) {
        EntityFluxRift rift = TTEntities.FLUX_RIFT.get().create(level, EntitySpawnReason.TRIGGERED);
        if (rift == null) {
            return null;
        }
        rift.restoreSnapshot(new RiftSnapshot(captured.identity(), level.dimension(), contents.revision(), destination, contents.size(), contents.seed(), contents.stability(), false, false));
        rift.transferToken(captured.token());
        rift.suspendTransfer();
        return level.addFreshEntity(rift) ? rift : null;
    }

    private static void settle(ServerLevel level, RiftTransferJournal.Entry entry, EntityFluxRift rift) throws IOException {
        level.save(null, true, false);
        RiftTransferJournal.write(level, new RiftTransferJournal.Entry(entry.captured(), entry.owner(), entry.source(), entry.target(), true));
        rift.resumeTransfer();
    }

    private static Optional<RiftTransferJournal.Entry> resolveCheckpoint(ServerLevel level, RiftTransferJournal.Entry entry) throws IOException {
        RiftTransferJournal.Checkpoint checkpoint = entry.checkpoint().orElseThrow();
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.owner());
        if (RiftPlayerCheckpoint.committed(level, entry.owner(), entry.captured().token())) {
            RiftTransferJournal.Entry committed = new RiftTransferJournal.Entry(entry.captured(), entry.owner(), entry.source(), entry.target(), entry.settled());
            RiftTransferJournal.write(level, committed);
            if (player != null) {
                RiftPlayerCheckpoint.clear(player, entry.captured().token());
            }
            return Optional.of(committed);
        }
        Optional<RiftTransferJournal.Entry> previous = checkpoint.previousToken()
                .map(token -> new RiftTransferJournal.Entry(new CapturedRift(entry.captured().version(), entry.captured().identity(), token, entry.captured().state()), entry.owner(), entry.source(),
                        Optional.of(entry.source()), true));
        RiftTransferJournal.restore(level, entry.captured().identity(), previous);
        if (player != null) {
            ItemStack jar = player.getInventory().getItem(checkpoint.jarSlot());
            if (entry.captured().equals(jar.get(TTDataComponents.CAPTURED_RIFT))) {
                player.getInventory().setItem(checkpoint.jarSlot(), checkpoint.beforeJar().copy());
            }
            if (ItemStack.matches(player.getInventory().getItem(checkpoint.wandSlot()), checkpoint.afterWand())) {
                player.getInventory().setItem(checkpoint.wandSlot(), checkpoint.beforeWand().copy());
            }
            RiftPlayerCheckpoint.clear(player, entry.captured().token());
            player.getInventory().setChanged();
        }
        EntityFluxRift source = existing(level, entry.captured().identity());
        if (source != null) {
            source.resumeTransfer();
        }
        return Optional.empty();
    }

    private static @Nullable EntityFluxRift existing(ServerLevel level, UUID identity) {
        for (ServerLevel dimension : level.getServer().getAllLevels()) {
            if (dimension.getEntity(identity) instanceof EntityFluxRift rift && !rift.isRemoved()) {
                return rift;
            }
        }
        return null;
    }

    private static @Nullable Container inventory(RiftOperationContext context) {
        if (context.player() != null) {
            return context.player().getInventory();
        }
        BlockPos pos = context.machinePosition();
        return pos != null && context.level().hasChunkAt(pos) && context.level().getBlockEntity(pos) instanceof Container container ? container : null;
    }

    private static ItemStack vessel(@Nullable Container inventory, int slot) {
        if (inventory == null || slot < 0 || slot >= inventory.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = inventory.getItem(slot);
        return stack.getCount() == 1 && stack.getItem() instanceof IRiftCaptureItem ? stack : ItemStack.EMPTY;
    }

    private static boolean current(Prepared prepared) {
        return inventory(prepared.context()) == prepared.inventory() && prepared.inventory().getItem(prepared.slot()) == prepared.stack() && ItemStack.matches(prepared.stack(), prepared.fingerprint())
                && prepared.policy().equals(((IRiftCaptureItem) prepared.stack().getItem()).capturePolicy());
    }

    private static boolean research(RiftOperationContext context, RiftCapturePolicy policy) {
        if (policy.research().isEmpty()) {
            return true;
        }
        ServerPlayer player = context.player();
        if (player == null) {
            player = RiftAuthorizations.actor(context).map(id -> context.level().getServer().getPlayerList().getPlayer(id)).orElse(null);
        }
        return player != null && KnowledgeAccess.of(player).isResearchComplete(policy.research().get());
    }
}
