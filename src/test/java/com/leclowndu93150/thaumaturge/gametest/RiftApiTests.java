package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.rifts.RiftAccess;
import com.leclowndu93150.thaumaturge.api.rifts.RiftChange;
import com.leclowndu93150.thaumaturge.api.rifts.RiftMutation;
import com.leclowndu93150.thaumaturge.api.rifts.RiftOperationContext;
import com.leclowndu93150.thaumaturge.api.rifts.RiftOperationEvent;
import com.leclowndu93150.thaumaturge.api.rifts.RiftResult;
import com.leclowndu93150.thaumaturge.api.rifts.RiftSnapshot;
import com.leclowndu93150.thaumaturge.api.rifts.RiftSpec;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestPlayers;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

public final class RiftApiTests {
    private static final Identifier REASON = Identifier.fromNamespaceAndPath("thaumaturge", "rift_api_test");

    private RiftApiTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("rift_api/transaction_and_stale", 20, helper -> {
            try (Fixture fixture = create(helper)) {
                RiftSnapshot original = fixture.snapshot();
                RiftChange change = new RiftChange(original.position(), 7, 42, 30.0F, false);
                Payment payment = new Payment();
                try (RiftMutation mutation = RiftAccess.prepareUpdate(fixture.context(), original, change)) {
                    check(mutation.result() == RiftResult.SUCCESS, "Rift update was rejected");
                    payment.updateSnapshots(mutation.transaction());
                    payment.balance -= 4;
                    try (RiftMutation concurrent = RiftAccess.prepareUpdate(fixture.context(), original, change)) {
                        check(concurrent.result() != RiftResult.SUCCESS, "Concurrent CAS accepted the same snapshot twice");
                    }
                }
                check(fixture.current().equals(original) && payment.balance == 10, "Abort did not restore rift and payment together");
                try (RiftMutation mutation = RiftAccess.prepareUpdate(fixture.context(), original, change)) {
                    check(mutation.result() == RiftResult.SUCCESS, "Rift update after abort was rejected");
                    payment.updateSnapshots(mutation.transaction());
                    payment.balance -= 4;
                    check(mutation.commit() == RiftResult.SUCCESS, "Rift commit failed");
                }
                RiftSnapshot updated = fixture.current();
                check(updated.size() == 7 && updated.seed() == 42 && updated.stability() == 30.0F && payment.balance == 6, "Commit did not apply rift and payment together");
                check(updated.identity().equals(original.identity()) && updated.revision() != original.revision() && !updated.busy(), "Commit lost identity or revision state");
                try (RiftMutation mutation = RiftAccess.prepareUpdate(fixture.context(), original, change)) {
                    check(mutation.result() == RiftResult.STALE, "Committed revision accepted a stale update");
                }
            }
            helper.succeed();
        });

        registrar.add("rift_api/commit_revalidates_permission_and_identity", 20, helper -> {
            try (Fixture fixture = create(helper)) {
                RiftSnapshot original = fixture.snapshot();
                RiftChange change = new RiftChange(original.position(), 7, 42, 30, false);
                Payment payment = new Payment();
                Consumer<RiftOperationEvent> deny = event -> {
                    if (event.context().equals(fixture.context())) {
                        event.setCanceled(true);
                    }
                };
                try (RiftMutation mutation = RiftAccess.prepareUpdate(fixture.context(), original, change)) {
                    check(mutation.result() == RiftResult.SUCCESS, "Reservation failed before permission change");
                    payment.updateSnapshots(mutation.transaction());
                    payment.balance -= 4;
                    NeoForge.EVENT_BUS.addListener(deny);
                    try {
                        check(mutation.commit() == RiftResult.DENIED, "Commit ignored permission revoked after reservation");
                    } finally {
                        NeoForge.EVENT_BUS.unregister(deny);
                    }
                }
                check(payment.balance == 10 && fixture.current().equals(original), "Denied commit did not restore payment and rift");
                try (RiftMutation mutation = RiftAccess.prepareUpdate(fixture.context(), original, change)) {
                    check(mutation.result() == RiftResult.SUCCESS, "Reservation failed before entity removal");
                    payment.updateSnapshots(mutation.transaction());
                    payment.balance -= 4;
                    helper.getLevel().getEntity(original.identity()).discard();
                    check(mutation.commit() != RiftResult.SUCCESS, "Commit accepted an entity removed after reservation");
                }
                check(payment.balance == 10 && RiftAccess.inspect(helper.getLevel(), original.identity()).isEmpty(), "Removed entity commit consumed payment or resurrected the entity");
            }
            helper.succeed();
        });

        registrar.add("rift_api/invalid_dimension_and_collapse", 20, helper -> {
            try (Fixture fixture = create(helper)) {
                RiftSnapshot original = fixture.snapshot();
                List<RiftChange> invalid = List.of(new RiftChange(new Vec3(Double.NaN, 0, 0), 5, 1, 0, false), new RiftChange(original.position(), Integer.MAX_VALUE, 1, 0, false),
                        new RiftChange(original.position(), 5, 1, Float.NaN, false), new RiftChange(original.position(), 5, 1, Float.POSITIVE_INFINITY, false));
                for (RiftChange change : invalid) {
                    try (RiftMutation mutation = RiftAccess.prepareUpdate(fixture.context(), original, change)) {
                        check(mutation.result() == RiftResult.INVALID, "Invalid rift change was accepted");
                    }
                }
                RiftSnapshot foreign = new RiftSnapshot(original.identity(), Level.NETHER, original.revision(), original.position(), original.size(), original.seed(), original.stability(), false,
                        false);
                try (RiftMutation mutation = RiftAccess.prepareUpdate(fixture.context(), foreign, RiftChange.of(original))) {
                    check(mutation.result() != RiftResult.SUCCESS, "Foreign dimension snapshot was accepted");
                }
                check(fixture.current().equals(original), "Rejected changes mutated the rift");
                RiftChange collapse = new RiftChange(original.position(), original.size(), original.seed(), original.stability(), true);
                try (RiftMutation mutation = RiftAccess.prepareUpdate(fixture.context(), original, collapse)) {
                    check(mutation.result() == RiftResult.SUCCESS, "Collapse request was rejected");
                    check(mutation.commit() == RiftResult.SUCCESS, "Collapse commit failed");
                }
                RiftSnapshot collapsing = fixture.current();
                check(collapsing.collapsing(), "Collapse was not committed");
                try (RiftMutation mutation = RiftAccess.prepareUpdate(fixture.context(), collapsing, RiftChange.of(original))) {
                    check(mutation.result() == RiftResult.COLLAPSING, "Collapsing rift accepted a mutation");
                }
            }
            helper.succeed();
        });

        registrar.add("rift_api/protected_destination_and_offline_actor", 20, helper -> {
            try (Fixture fixture = create(helper)) {
                RiftSnapshot original = fixture.snapshot();
                Vec3 destination = original.position().add(2, 0, 0);
                AtomicBoolean checkedVolumes = new AtomicBoolean();
                Consumer<RiftOperationEvent> deny = event -> {
                    if (event.context().equals(fixture.context())) {
                        checkedVolumes.set(event.source().contains(original.position()) && event.destination().contains(destination));
                        event.setCanceled(true);
                    }
                };
                NeoForge.EVENT_BUS.addListener(deny);
                RiftChange move = new RiftChange(destination, original.size(), original.seed(), original.stability(), false);
                try (RiftMutation mutation = RiftAccess.prepareUpdate(fixture.context(), original, move)) {
                    check(mutation.result() == RiftResult.DENIED, "Destination protection was ignored");
                } finally {
                    NeoForge.EVENT_BUS.unregister(deny);
                }
                check(checkedVolumes.get(), "Protection event did not expose both affected volumes");
                ServerPlayer absentPlayer = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "rift_offline"));
                RiftOperationContext absent = RiftOperationContext.player(absentPlayer, REASON);
                try (RiftMutation mutation = RiftAccess.prepareUpdate(absent, original, RiftChange.of(original))) {
                    check(mutation.result() == RiftResult.DENIED, "Offline actor UUID authorized a mutation");
                }
                check(fixture.current().equals(original), "Denied update changed the rift");
            }
            helper.succeed();
        });

        registrar.add("rift_api/query_guards", 20, helper -> {
            try (Fixture fixture = create(helper)) {
                Vec3 position = fixture.snapshot().position();
                AABB bounds = new AABB(position.subtract(2, 2, 2), position.add(2, 2, 2));
                check(RiftAccess.query(helper.getLevel(), bounds, 1).size() == 1, "Bounded query did not find its rift");
                check(RiftAccess.query(helper.getLevel(), bounds, 0).isEmpty(), "Zero query limit was accepted");
                check(RiftAccess.query(helper.getLevel(), bounds, 65).isEmpty(), "Excessive query limit was accepted");
                check(RiftAccess.query(helper.getLevel(), bounds.inflate(100), 1).isEmpty(), "Excessive query volume was accepted");
                BlockPos distant = new BlockPos(29_000_000, 100, 29_000_000);
                check(!helper.getLevel().hasChunkAt(distant), "Test destination is already loaded");
                check(RiftAccess.query(helper.getLevel(), new AABB(distant).inflate(2), 1).isEmpty(), "Unloaded query returned a rift");
                check(!helper.getLevel().hasChunkAt(distant), "Query force-loaded its chunk");
                AtomicBoolean hidden = new AtomicBoolean();
                Thread thread = Thread.ofVirtual().start(() -> hidden.set(RiftAccess.inspect(helper.getLevel(), fixture.snapshot().identity()).isEmpty()));
                try {
                    thread.join();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
                check(hidden.get(), "Off-thread inspection accessed a live rift");
            }
            helper.succeed();
        });

        registrar.add("rift_api/native_revision_and_removal", 20, helper -> {
            try (Fixture fixture = create(helper)) {
                RiftSnapshot original = fixture.snapshot();
                var entity = helper.getLevel().getEntity(original.identity());
                entity.setPos(original.position().add(0, 1, 0));
                RiftSnapshot moved = fixture.current();
                check(moved.revision() != original.revision(), "Native entity movement did not invalidate the snapshot");
                try (RiftMutation mutation = RiftAccess.prepareUpdate(fixture.context(), original, RiftChange.of(original))) {
                    check(mutation.result() == RiftResult.STALE, "Native movement accepted an obsolete snapshot");
                }
                entity.discard();
                check(RiftAccess.inspect(helper.getLevel(), original.identity()).isEmpty(), "Removed rift remained visible");
                try (RiftMutation mutation = RiftAccess.prepareUpdate(fixture.context(), moved, RiftChange.of(moved))) {
                    check(mutation.result() == RiftResult.UNLOADED, "Removed rift accepted a mutation");
                }
            }
            helper.succeed();
        });

        registrar.add("rift_api/creation_simulation_and_payment", 20, helper -> {
            ServerPlayer player = TTTestPlayers.create(helper);
            Vec3 position = Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 4, 2)));
            BlockPos blockPos = BlockPos.containing(position);
            float originalFlux = AuraHelper.getFlux(helper.getLevel(), blockPos);
            RiftOperationContext context = RiftOperationContext.player(player, REASON);
            AABB bounds = new AABB(position.subtract(2, 2, 2), position.add(2, 2, 2));
            try {
                AuraHelper.drainFlux(helper.getLevel(), blockPos, originalFlux, false);
                RiftSpec spec = new RiftSpec(position, 5, 37, 0, RiftSpec.Source.FLUX);
                check(RiftAccess.simulateCreation(context, spec) == RiftResult.INSUFFICIENT_PAYMENT, "Creation ignored insufficient flux");
                AuraHelper.addFlux(helper.getLevel(), blockPos, 10);
                check(RiftAccess.simulateCreation(context, spec) == RiftResult.SUCCESS, "Funded creation simulation failed");
                check(AuraHelper.getFlux(helper.getLevel(), blockPos) == 10 && RiftAccess.query(helper.getLevel(), bounds, 64).isEmpty(), "Simulation changed flux or spawned a rift");
                Consumer<EntityJoinLevelEvent> denySpawn = event -> {
                    if (event.getLevel() == helper.getLevel() && event.getEntity().position().equals(position)) {
                        event.setCanceled(true);
                    }
                };
                NeoForge.EVENT_BUS.addListener(denySpawn);
                try {
                    check(RiftAccess.tryCreate(context, spec) == RiftResult.DENIED, "Canceled rift spawn reported success");
                    check(AuraHelper.getFlux(helper.getLevel(), blockPos) == 10 && RiftAccess.query(helper.getLevel(), bounds, 64).isEmpty(), "Canceled spawn did not roll back flux payment");
                } finally {
                    NeoForge.EVENT_BUS.unregister(denySpawn);
                }
                check(RiftAccess.tryCreate(context, spec) == RiftResult.SUCCESS, "Funded creation failed");
                check(AuraHelper.getFlux(helper.getLevel(), blockPos) == 5 && RiftAccess.query(helper.getLevel(), bounds, 64).size() == 1, "Creation did not consume flux exactly once");
            } finally {
                for (RiftSnapshot snapshot : RiftAccess.query(helper.getLevel(), bounds, 64)) {
                    var entity = helper.getLevel().getEntity(snapshot.identity());
                    if (entity != null) {
                        entity.discard();
                    }
                }
                AuraHelper.drainFlux(helper.getLevel(), blockPos, AuraHelper.getFlux(helper.getLevel(), blockPos), false);
                AuraHelper.addFlux(helper.getLevel(), blockPos, originalFlux);
                helper.getLevel().getServer().getPlayerList().remove(player);
            }
            helper.succeed();
        });
    }

    private static Fixture create(GameTestHelper helper) {
        ServerPlayer player = TTTestPlayers.create(helper);
        Vec3 position = Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 4, 2)));
        RiftOperationContext context = RiftOperationContext.player(player, REASON);
        RiftResult result = RiftAccess.tryCreate(context, new RiftSpec(position, 5, 37, 0, RiftSpec.Source.CREATIVE));
        if (result != RiftResult.SUCCESS) {
            helper.getLevel().getServer().getPlayerList().remove(player);
            throw new IllegalStateException("Rift fixture creation failed: " + result);
        }
        RiftSnapshot snapshot = RiftAccess.query(helper.getLevel(), new AABB(position.subtract(2, 2, 2), position.add(2, 2, 2)), 64).stream().filter(found -> found.position().equals(position))
                .findFirst().orElseThrow();
        return new Fixture(player, context, snapshot);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private record Fixture(ServerPlayer player, RiftOperationContext context, RiftSnapshot snapshot) implements AutoCloseable {
        RiftSnapshot current() {
            return RiftAccess.inspect(context.level(), snapshot.identity()).orElseThrow();
        }

        @Override
        public void close() {
            var entity = context.level().getEntity(snapshot.identity());
            if (entity != null) {
                entity.discard();
            }
            context.level().getServer().getPlayerList().remove(player);
        }
    }

    private static final class Payment extends SnapshotJournal<Integer> {
        private int balance = 10;

        @Override
        protected Integer createSnapshot() {
            return balance;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            balance = snapshot;
        }
    }
}
