package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastAuthority;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellAuthorities;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellContinuation;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.event.SpellNodeEvent;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestPlayers;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

public final class SpellAuthorityEngineTests {
    private SpellAuthorityEngineTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("spell/authorized_engine_payment_and_continuation", 40, helper -> {
            var owner = TTTestPlayers.create(helper);
            var level = helper.getLevel();
            var source = new TestSource(level, owner.getUUID());
            source.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 1, 1))));
            level.addFreshEntity(source);
            var scope = TTIds.rl("gametest/engine_" + UUID.randomUUID());
            SpellAuthorities.register(scope, 1, (world, authority, machine, position, target) -> true);
            AtomicInteger payments = new AtomicInteger();
            AtomicInteger executions = new AtomicInteger();
            AtomicReference<SpellContinuation> captured = new AtomicReference<>();
            AtomicReference<CastAuthority> observed = new AtomicReference<>();
            Consumer<SpellNodeEvent> listener = event -> {
                if (event.context().caster() == source) {
                    executions.incrementAndGet();
                    observed.set(event.context().authority().orElse(null));
                    if (event.context().hasNext()) {
                        captured.set(event.context().continuation());
                    }
                    event.setCanceled(true);
                }
            };
            NeoForge.EVENT_BUS.addListener(SpellNodeEvent.class, listener);
            try {
                CastAuthority authority = SpellAuthorities.grant(owner, source, scope, 8, 100, false).orElseThrow();
                Spell spell = Spell.empty().withRoot(SpellNode.of(Spell.ORIGIN).then(SpellNode.of(Spell.ORIGIN)));
                var origins = List.of(SpellTarget.origin(source));
                if (!Spells.castAuthorized(source, ItemStack.EMPTY, spell, origins, 1, authority, () -> payments.incrementAndGet() == 1)
                        || Spells.castAuthorized(source, ItemStack.EMPTY, spell, origins, 1, authority, () -> payments.incrementAndGet() > 0) || payments.get() != 1 || executions.get() != 1
                        || !authority.equals(observed.get()) || captured.get() == null) {
                    helper.fail("The public casting engine lost authority, duplicated payment, or failed to issue a continuation");
                    return;
                }
                SpellContinuation continuation = captured.get();
                var serialized = SpellContinuation.CODEC.encodeStart(NbtOps.INSTANCE, continuation).getOrThrow();
                continuation = SpellContinuation.CODEC.parse(NbtOps.INSTANCE, serialized).getOrThrow();
                Spells.resume(level.getServer().getLevel(Level.NETHER), continuation, origins);
                if (executions.get() != 1) {
                    helper.fail("A continuation executed in another dimension");
                    return;
                }
                Spells.resume(level, continuation, origins);
                Spells.resume(level, continuation, origins);
                if (executions.get() != 2 || payments.get() != 1 || !authority.equals(observed.get())) {
                    helper.fail("A resumed continuation duplicated execution or payment, or lost authority");
                    return;
                }
                var next = new SpellContinuation(continuation.nodes(), continuation.state(), continuation.caster(), continuation.castId(), continuation.authority(), continuation.token(), 1);
                if (!SpellAuthorities.revoke(owner, authority)) {
                    helper.fail("The owner could not revoke the engine grant");
                    return;
                }
                Spells.resume(level, next, origins);
                Spells.cast(source, ItemStack.EMPTY, spell, origins, 1);
                if (executions.get() != 2 || payments.get() != 1) {
                    helper.fail("A revoked continuation or unauthorised machine cast executed");
                    return;
                }
                CastAuthority denied = SpellAuthorities.grant(owner, source, scope, 8, 100, false).orElseThrow();
                if (Spells.castAuthorized(source, ItemStack.EMPTY, spell, origins, 1, denied, () -> {
                    payments.incrementAndGet();
                    return false;
                }) || Spells.castAuthorized(source, ItemStack.EMPTY, spell, origins, 1, denied, () -> payments.incrementAndGet() > 0) || payments.get() != 2 || executions.get() != 2) {
                    helper.fail("Failed payment executed a spell or allowed the grant to be charged again");
                    return;
                }
                SpellAuthorities.revoke(owner, denied);
                helper.succeed();
            } finally {
                NeoForge.EVENT_BUS.unregister(listener);
                source.discard();
                level.getServer().getPlayerList().remove(owner);
            }
        });

        registrar.add("spell/continuation_persistence_and_legacy_codecs", 40, helper -> {
            var authority = new CastAuthority(UUID.randomUUID(), UUID.randomUUID(), Level.OVERWORLD.identifier(), TTIds.rl("gametest/codec"), UUID.randomUUID(), 1, UUID.randomUUID());
            var continuation = new SpellContinuation(List.of(SpellNode.of(Spell.ORIGIN)), SpellState.DEFAULT, Optional.of(authority.sourceId()), authority.castId(), Optional.of(authority),
                    Optional.of(UUID.randomUUID()), 4);
            var saved = SpellContinuation.CODEC.encodeStart(NbtOps.INSTANCE, continuation).getOrThrow();
            if (!continuation.equals(SpellContinuation.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow())) {
                helper.fail("Modern continuation persistence lost authority or replay sequence");
                return;
            }
            ByteBuf buffer = Unpooled.buffer();
            try {
                SpellContinuation.STREAM_CODEC.encode(buffer, continuation);
                if (!continuation.equals(SpellContinuation.STREAM_CODEC.decode(buffer))) {
                    helper.fail("Modern continuation network roundtrip changed the payload");
                    return;
                }
                var legacy = new SpellContinuation(continuation.nodes(), continuation.state(), continuation.caster(), continuation.castId());
                var legacyJson = SpellContinuation.CODEC.encodeStart(JsonOps.INSTANCE, legacy).getOrThrow().getAsJsonObject();
                legacyJson.remove("authority");
                legacyJson.remove("token");
                legacyJson.remove("sequence");
                legacyJson.remove("version");
                var decodedLegacy = SpellContinuation.CODEC.parse(JsonOps.INSTANCE, legacyJson).getOrThrow();
                if (!legacyPayloadMatches(legacy, decodedLegacy)) {
                    helper.fail("Legacy continuation data no longer decodes with empty authority");
                    return;
                }
                buffer.clear();
                SpellNode.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, legacy.nodes());
                SpellState.STREAM_CODEC.encode(buffer, legacy.state());
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).encode(buffer, legacy.caster());
                UUIDUtil.STREAM_CODEC.encode(buffer, legacy.castId());
                if (!legacyPayloadMatches(legacy, SpellContinuation.STREAM_CODEC.decode(buffer))) {
                    helper.fail("Legacy network continuation no longer decodes");
                    return;
                }
                var modernJson = SpellContinuation.CODEC.encodeStart(JsonOps.INSTANCE, continuation).getOrThrow().getAsJsonObject();
                var missingToken = modernJson.deepCopy();
                missingToken.remove("token");
                var wrongCast = modernJson.deepCopy();
                wrongCast.add("cast", UUIDUtil.CODEC.encodeStart(JsonOps.INSTANCE, UUID.randomUUID()).getOrThrow());
                var wrongCaster = modernJson.deepCopy();
                wrongCaster.add("caster", UUIDUtil.CODEC.encodeStart(JsonOps.INSTANCE, UUID.randomUUID()).getOrThrow());
                if (SpellContinuation.CODEC.parse(JsonOps.INSTANCE, missingToken).error().isEmpty() || SpellContinuation.CODEC.parse(JsonOps.INSTANCE, wrongCast).error().isEmpty()
                        || SpellContinuation.CODEC.parse(JsonOps.INSTANCE, wrongCaster).error().isEmpty()) {
                    helper.fail("Malformed continuation identity did not return a decoding error");
                    return;
                }
                legacyJson.addProperty("version", 2);
                if (SpellContinuation.CODEC.parse(JsonOps.INSTANCE, legacyJson).isSuccess()) {
                    helper.fail("An unknown persisted continuation version was accepted");
                    return;
                }
                buffer.clear();
                ByteBufCodecs.VAR_INT.encode(buffer, -1);
                ByteBufCodecs.VAR_INT.encode(buffer, 2);
                boolean rejected = false;
                try {
                    SpellContinuation.STREAM_CODEC.decode(buffer);
                } catch (IllegalArgumentException expected) {
                    rejected = true;
                }
                if (!rejected) {
                    helper.fail("An unknown network continuation version was accepted");
                    return;
                }
                helper.succeed();
            } finally {
                buffer.release();
            }
        });

        registrar.add("spell/native_legacy_continuation_execution", 40, helper -> {
            var owner = TTTestPlayers.create(helper);
            AtomicInteger executions = new AtomicInteger();
            UUID detachedCast = UUID.randomUUID();
            Consumer<SpellNodeEvent> listener = event -> {
                if ((event.context().caster() == owner || event.context().castId().equals(detachedCast)) && event.context().authority().isEmpty()) {
                    executions.incrementAndGet();
                    event.setCanceled(true);
                }
            };
            NeoForge.EVENT_BUS.addListener(SpellNodeEvent.class, listener);
            try {
                Spells.cast(owner, Spell.empty(), 1);
                var legacy = new SpellContinuation(List.of(SpellNode.of(Spell.ORIGIN)), SpellState.DEFAULT, Optional.of(owner.getUUID()), UUID.randomUUID());
                Spells.resume(helper.getLevel(), legacy, List.of(SpellTarget.origin(owner)));
                var legacyJson = SpellContinuation.CODEC.encodeStart(JsonOps.INSTANCE, legacy).getOrThrow().getAsJsonObject();
                legacyJson.remove("version");
                Spells.resume(helper.getLevel(), SpellContinuation.CODEC.parse(JsonOps.INSTANCE, legacyJson).getOrThrow(), List.of(SpellTarget.origin(owner)));
                var detached = new SpellContinuation(List.of(SpellNode.of(Spell.ORIGIN)), SpellState.DEFAULT, Optional.empty(), detachedCast);
                var detachedData = SpellContinuation.CODEC.encodeStart(NbtOps.INSTANCE, detached).getOrThrow();
                Spells.resume(helper.getLevel(), SpellContinuation.CODEC.parse(NbtOps.INSTANCE, detachedData).getOrThrow(), List.of(SpellTarget.origin(owner)));
                if (executions.get() != 4) {
                    helper.fail("Native casts or legacy continuations stopped executing without machine authority");
                    return;
                }
                helper.succeed();
            } finally {
                NeoForge.EVENT_BUS.unregister(listener);
                helper.getLevel().getServer().getPlayerList().remove(owner);
            }
        });
    }

    private static boolean legacyPayloadMatches(SpellContinuation expected, SpellContinuation actual) {
        return actual.legacy() && actual.authority().isEmpty() && actual.token().isEmpty() && actual.sequence() == 0 && expected.nodes().equals(actual.nodes())
                && expected.state().equals(actual.state()) && expected.caster().equals(actual.caster()) && expected.castId().equals(actual.castId());
    }

    private static final class TestSource extends Zombie implements SpellAuthorities.Source {
        private final UUID owner;

        private TestSource(ServerLevel level, UUID owner) {
            super(EntityType.ZOMBIE, level);
            this.owner = owner;
        }

        @Override
        public UUID spellOwner() {
            return owner;
        }
    }
}
