package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.api.aura.AuraBaseline;
import com.leclowndu93150.thaumaturge.api.aura.AuraBaseline.Context;
import com.leclowndu93150.thaumaturge.api.aura.AuraBaseline.Result;
import com.leclowndu93150.thaumaturge.api.aura.AuraBaselineEvent;
import com.leclowndu93150.thaumaturge.api.aura.IAuraBaselineMachine;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class AuraBaselineTests {
    private AuraBaselineTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("aura/baseline_preserves_pools_and_native_sampler", 20, helper -> {
            Fixture fixture = fixture(helper);
            try {
                var quote = AuraBaseline.quote(fixture.context()).orElseThrow();
                helper.assertTrue(quote.proposedBase() == AuraGenHandler.generateBase(helper.getLevel(), fixture.chunk().getPos()), "Baseline must use native deterministic generation");
                try (Transaction transaction = Transaction.openRoot()) {
                    helper.assertTrue(AuraBaseline.recalculate(fixture.context(), quote, 1, transaction) == Result.APPLIED, "Recalculation must stage");
                    transaction.commit();
                }
                helper.assertTrue(fixture.data().getBase() == quote.proposedBase() && fixture.data().getVis() == 900 && fixture.data().getFlux() == 37,
                        "Recalculation must preserve over-capacity vis and flux");
                try (Transaction transaction = Transaction.openRoot()) {
                    helper.assertTrue(AuraBaseline.recalculate(fixture.context(), quote, 1, transaction) == Result.ALREADY_APPLIED, "Retry must acknowledge receipt before stale quote");
                }
                helper.succeed();
            } finally {
                fixture.restore();
            }
        });
        registrar.add("aura/baseline_payment_rollback_and_stale", 20, helper -> {
            Fixture fixture = fixture(helper);
            try {
                var quote = AuraBaseline.quote(fixture.context()).orElseThrow();
                try (Transaction transaction = Transaction.openRoot()) {
                    helper.assertTrue(AuraBaseline.recalculate(fixture.context(), quote, 1, transaction) == Result.APPLIED, "Recalculation must stage");
                    fixture.data().setVis(899, transaction);
                }
                helper.assertTrue(fixture.data().getBase() == 400 && fixture.data().getVis() == 900 && fixture.data().lastBaselineOperation(fixture.player().getUUID()) == 0,
                        "Aborted payment must restore baseline, receipt and vis");
                fixture.data().setBase((short) 401);
                try (Transaction transaction = Transaction.openRoot()) {
                    helper.assertTrue(AuraBaseline.recalculate(fixture.context(), quote, 1, transaction) == Result.STALE, "Changed baseline must reject stale quote");
                }
                helper.succeed();
            } finally {
                fixture.restore();
            }
        });
        registrar.add("aura/baseline_receipt_survives_reload", 20, helper -> {
            Fixture fixture = fixture(helper);
            try {
                var quote = AuraBaseline.quote(fixture.context()).orElseThrow();
                try (Transaction transaction = Transaction.openRoot()) {
                    helper.assertTrue(AuraBaseline.recalculate(fixture.context(), quote, 5, transaction) == Result.APPLIED, "Recalculation must stage");
                    transaction.commit();
                }
                var encoded = AuraData.CODEC.codec().encodeStart(JsonOps.INSTANCE, fixture.data()).getOrThrow();
                AuraData restored = AuraData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();
                fixture.chunk().setData(TTAttachments.AURA.get(), restored);
                try (Transaction transaction = Transaction.openRoot()) {
                    helper.assertTrue(AuraBaseline.recalculate(fixture.context(), quote, 5, transaction) == Result.ALREADY_APPLIED, "Reload must not charge a committed sequence again");
                    helper.assertTrue(AuraBaseline.recalculate(fixture.context(), quote, 4, transaction) == Result.STALE, "Older sequence must remain rejected");
                }
                helper.assertTrue(restored.getVis() == 900 && restored.getFlux() == 37, "Receipt reload must preserve pools");
                helper.succeed();
            } finally {
                fixture.restore();
            }
        });
        registrar.add("aura/baseline_protection_and_unloaded_target", 20, helper -> {
            Fixture fixture = fixture(helper);
            Consumer<AuraBaselineEvent> deny = event -> {
                if (event.context().equals(fixture.context())) {
                    event.setCanceled(true);
                }
            };
            try {
                var quote = AuraBaseline.quote(fixture.context()).orElseThrow();
                NeoForge.EVENT_BUS.addListener(deny);
                try (Transaction transaction = Transaction.openRoot()) {
                    helper.assertTrue(AuraBaseline.recalculate(fixture.context(), quote, 1, transaction) == Result.DENIED, "Protection must be checked at commit");
                }
                ChunkPos unloaded = new ChunkPos(1000000, 1000000);
                helper.assertTrue(helper.getLevel().getChunkSource().getChunkNow(unloaded.x(), unloaded.z()) == null, "Fixture chunk must be unloaded");
                helper.assertTrue(AuraBaseline.quote(Context.player(fixture.player(), unloaded)).isEmpty(), "Unloaded target must not produce a quote");
                helper.assertTrue(helper.getLevel().getChunkSource().getChunkNow(unloaded.x(), unloaded.z()) == null, "Quoting must not load a chunk");
                helper.succeed();
            } finally {
                NeoForge.EVENT_BUS.unregister(deny);
                fixture.restore();
            }
        });
        registrar.add("aura/baseline_delegation_revocation_and_identity", 20, helper -> {
            Fixture fixture = fixture(helper);
            BlockPos position = helper.absolutePos(new BlockPos(2, 2, 2));
            AtomicBoolean revokeDuringEvent = new AtomicBoolean();
            Consumer<AuraBaselineEvent> allow = event -> {
                if (event.context().level() == helper.getLevel() && event.context().chunk().equals(fixture.chunk().getPos())) {
                    event.setCanceled(false);
                    if (revokeDuringEvent.get()) {
                        event.context().delegation().ifPresent(delegation -> AuraBaseline.revoke(fixture.player(), delegation));
                    }
                }
            };
            UUID token = null;
            try {
                helper.getLevel().setBlockAndUpdate(position, Blocks.CHEST.defaultBlockState());
                Machine machine = new Machine(position, fixture.player().getUUID());
                helper.getLevel().setBlockEntity(machine);
                token = AuraBaseline.delegate(fixture.player(), position, fixture.chunk().getPos(), 1200).orElseThrow();
                Context context = Context.delegated(helper.getLevel(), fixture.chunk().getPos(), token);
                helper.assertTrue(AuraBaseline.quote(context).isEmpty(), "Offline access must fail closed without protection authorization");
                NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, true, allow);
                helper.assertTrue(AuraBaseline.quote(context).isPresent(), "Explicitly authorized machine must resolve");
                UUID originalIdentity = machine.identity;
                machine.identity = UUID.randomUUID();
                helper.assertTrue(AuraBaseline.quote(context).isEmpty(), "Replaced machine must not inherit a grant");
                machine.identity = originalIdentity;
                revokeDuringEvent.set(true);
                helper.assertTrue(AuraBaseline.quote(context).isEmpty(), "Revocation by an authorization listener must take effect immediately");
                helper.assertTrue(AuraBaseline.quote(context).isEmpty(), "Revoked grant must remain unusable");
                helper.succeed();
            } finally {
                if (token != null) {
                    AuraBaseline.revoke(fixture.player(), token);
                }
                NeoForge.EVENT_BUS.unregister(allow);
                fixture.restore();
            }
        });
    }

    private static Fixture fixture(GameTestHelper helper) {
        BlockPos position = helper.absolutePos(new BlockPos(2, 2, 2));
        ChunkPos center = new ChunkPos(position.getX() >> 4, position.getZ() >> 4);
        for (int[] offset : new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            helper.getLevel().getChunk(center.x() + offset[0], center.z() + offset[1]);
        }
        LevelChunk chunk = helper.getLevel().getChunkAt(position);
        AuraData original = chunk.getData(TTAttachments.AURA.get());
        AuraData data = new AuraData((short) 400, 900, 37);
        chunk.setData(TTAttachments.AURA.get(), data);
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "aura_baseline"));
        player.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        return new Fixture(chunk, original, data, player, Context.player(player, center));
    }

    private record Fixture(LevelChunk chunk, AuraData original, AuraData data, ServerPlayer player, Context context) {
        void restore() {
            chunk.setData(TTAttachments.AURA.get(), original);
        }
    }

    private static final class Machine extends BlockEntity implements IAuraBaselineMachine {
        private UUID identity = UUID.randomUUID();
        private final UUID owner;

        private Machine(BlockPos position, UUID owner) {
            super(BlockEntityType.CHEST, position, Blocks.CHEST.defaultBlockState());
            this.owner = owner;
        }

        @Override
        public UUID auraMachineIdentity() {
            return identity;
        }

        @Override
        public UUID auraMachineOwner() {
            return owner;
        }
    }
}
