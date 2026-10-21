package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastAuthority;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellAuthorities;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestPlayers;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class SpellAuthorityTests {
    private SpellAuthorityTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("spell/authority_resolver_revalidation", 40, helper -> {
            var owner = TTTestPlayers.create(helper);
            var level = helper.getLevel();
            var source = new TestSource(level, owner.getUUID());
            source.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 1, 1))));
            level.addFreshEntity(source);
            var scope = TTIds.rl("gametest/authority_revalidation_" + UUID.randomUUID());
            AtomicInteger mutation = new AtomicInteger();
            SpellAuthorities.register(scope, 1, (world, authority, machine, position, target) -> {
                if (mutation.get() == 1) {
                    SpellAuthorities.revoke(owner, authority);
                } else if (mutation.get() == 2) {
                    source.owner = UUID.randomUUID();
                } else if (mutation.get() == 3) {
                    owner.getAbilities().mayBuild = false;
                }
                return true;
            });
            boolean mayBuild = owner.getAbilities().mayBuild;
            try {
                mutation.set(1);
                helper.assertTrue(SpellAuthorities.grant(owner, source, scope, 8, 100, false).isEmpty(), "Resolver revocation during grant must fail closed");
                for (int operation = 0; operation < 4; operation++) {
                    mutation.set(0);
                    var authority = SpellAuthorities.grant(owner, source, scope, 8, 100, false).orElseThrow();
                    helper.assertTrue(CastAuthorities.begin(source, authority, () -> true), "Revalidation fixture must activate");
                    var token = CastAuthorities.issue(level, authority).orElseThrow();
                    mutation.set(operation < 2 ? 1 : operation);
                    boolean accepted = operation == 0
                            ? CastAuthorities.issue(level, authority).isPresent()
                            : operation == 1 ? CastAuthorities.claim(level, authority, token, 0) : CastAuthorities.valid(level, authority);
                    helper.assertTrue(!accepted, "Resolver side effects must invalidate the current operation");
                    source.owner = owner.getUUID();
                    owner.getAbilities().mayBuild = mayBuild;
                    SpellAuthorities.revoke(owner, authority);
                }
                mutation.set(0);
                owner.getAbilities().mayBuild = false;
                helper.assertTrue(SpellAuthorities.grant(owner, source, scope, 8, 100, false).isEmpty(), "An owner without build permission must not authorize machine mutations");
                helper.succeed();
            } finally {
                owner.getAbilities().mayBuild = mayBuild;
                source.discard();
                level.getServer().getPlayerList().remove(owner);
            }
        });
        registrar.add("spell/authority_payment_scope_and_replay", 40, helper -> {
            var owner = TTTestPlayers.create(helper);
            var level = helper.getLevel();
            var source = new TestSource(level, owner.getUUID());
            source.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 1, 1))));
            level.addFreshEntity(source);
            var scope = TTIds.rl("gametest/authority_" + UUID.randomUUID());
            SpellAuthorities.register(scope, 1, (world, authority, machine, position, target) -> true);
            try {
                CastAuthority authority = SpellAuthorities.grant(owner, source, scope, 8, 100, false).orElseThrow();
                var encoded = CastAuthority.CODEC.encodeStart(NbtOps.INSTANCE, authority).getOrThrow();
                if (!authority.equals(CastAuthority.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow())) {
                    helper.fail("Authority changed during persistence roundtrip");
                    return;
                }
                AtomicInteger payments = new AtomicInteger();
                if (!CastAuthorities.begin(source, authority, () -> payments.incrementAndGet() == 1) || CastAuthorities.begin(source, authority, () -> payments.incrementAndGet() > 0)
                        || payments.get() != 1) {
                    helper.fail("An automated cast did not charge exactly once");
                    return;
                }
                UUID token = CastAuthorities.issue(level, authority).orElseThrow();
                if (!CastAuthorities.claim(level, authority, token, 0) || CastAuthorities.claim(level, authority, token, 0) || CastAuthorities.claim(level, authority, token, 2)
                        || !CastAuthorities.claim(level, authority, token, 1)) {
                    helper.fail("Continuation replay or out-of-order emission was accepted");
                    return;
                }
                if (CastAuthorities.valid(level, authority, source.position().add(9, 0, 0), null)) {
                    helper.fail("An effect escaped the authorized scope");
                    return;
                }
                var data = CastAuthorityData.get(level.getServer());
                var saved = CastAuthorityData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
                var restored = CastAuthorityData.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
                data.grants.clear();
                data.grants.putAll(restored.grants);
                data.setDirty();
                if (CastAuthorities.claim(level, authority, token, 1) || !CastAuthorities.claim(level, authority, token, 2)
                        || CastAuthorities.begin(source, authority, () -> payments.incrementAndGet() > 0) || payments.get() != 1) {
                    helper.fail("Ledger reload lost payment or emission replay protection");
                    return;
                }
                if (CastAuthorities.valid(level.getServer().getLevel(Level.NETHER), authority)) {
                    helper.fail("Authority crossed its originating dimension");
                    return;
                }
                if (!SpellAuthorities.revoke(owner, authority) || CastAuthorities.valid(level, authority) || CastAuthorities.claim(level, authority, token, 3)) {
                    helper.fail("Revoked authority remained executable");
                    return;
                }
                CastAuthority replaced = SpellAuthorities.grant(owner, source, scope, 8, 100, false).orElseThrow();
                if (!CastAuthorities.begin(source, replaced, () -> true)) {
                    helper.fail("Replacement fixture could not start");
                    return;
                }
                source.owner = UUID.randomUUID();
                if (CastAuthorities.valid(level, replaced)) {
                    helper.fail("A replacement owner retained authority");
                    return;
                }
                source.owner = owner.getUUID();
                if (CastAuthorities.valid(level, replaced)) {
                    helper.fail("Restoring an owner revived revoked authority");
                    return;
                }
                CastAuthority onlineOnly = SpellAuthorities.grant(owner, source, scope, 8, 100, false).orElseThrow();
                if (!CastAuthorities.begin(source, onlineOnly, () -> true)) {
                    helper.fail("A fresh online grant could not start");
                    return;
                }
                CastAuthority offline = SpellAuthorities.grant(owner, source, scope, 8, 100, true).orElseThrow();
                if (!CastAuthorities.begin(source, offline, () -> true)) {
                    helper.fail("An explicitly offline grant could not start");
                    return;
                }
                level.getServer().getPlayerList().remove(owner);
                if (CastAuthorities.valid(level, onlineOnly) || !CastAuthorities.valid(level, offline)) {
                    helper.fail("Owner logout did not respect explicit offline policy");
                    return;
                }
                source.discard();
                if (CastAuthorities.valid(level, offline)) {
                    helper.fail("A removed source retained cast authority");
                    return;
                }
                helper.succeed();
            } finally {
                source.discard();
                if (level.getServer().getPlayerList().getPlayer(owner.getUUID()) != null) {
                    level.getServer().getPlayerList().remove(owner);
                }
            }
        });
    }

    private static final class TestSource extends Zombie implements SpellAuthorities.Source {
        private UUID owner;

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
