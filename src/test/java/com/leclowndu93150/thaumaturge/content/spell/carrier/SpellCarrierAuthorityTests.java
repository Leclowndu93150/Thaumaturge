package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellAuthorities;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellContinuation;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.event.SpellNodeEvent;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SpellLook;
import com.leclowndu93150.thaumaturge.content.spell.engine.CastAuthorities;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestPlayers;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

public final class SpellCarrierAuthorityTests {
    private SpellCarrierAuthorityTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("spell/carrier_authority_reload_and_fanout", 40, helper -> {
            var level = helper.getLevel();
            var owner = TTTestPlayers.create(helper);
            var source = new TestSource(level, owner.getUUID());
            source.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 1, 1))));
            level.addFreshEntity(source);
            var scope = Identifier.fromNamespaceAndPath("thaumaturge_test", "carrier_" + UUID.randomUUID());
            SpellAuthorities.register(scope, 1, (world, authority, machine, position, target) -> true);
            AtomicInteger emissions = new AtomicInteger();
            Consumer<SpellNodeEvent> listener = event -> {
                if (event.context().caster() == source) {
                    emissions.incrementAndGet();
                    event.setCanceled(true);
                }
            };
            NeoForge.EVENT_BUS.addListener(SpellNodeEvent.class, listener);
            try {
                var authority = SpellAuthorities.grant(owner, source, scope, 8, 100, false).orElseThrow();
                helper.assertTrue(CastAuthorities.begin(source, authority, () -> true), "Authorized cast must activate before carrier creation");
                var continuation = new SpellContinuation(List.of(SpellNode.of(Spell.ORIGIN)), SpellState.DEFAULT, Optional.of(source.getUUID()), authority.castId(), Optional.of(authority),
                        CastAuthorities.issue(level, authority), 0);
                var payload = new CarrierPayload(continuation, SpellLook.DEFAULT);
                var first = new CarrierCharge();
                var second = new CarrierCharge();
                first.arm(level, payload);
                second.arm(level, payload);
                helper.assertTrue(!first.payload().continuation().token().equals(second.payload().continuation().token()), "Independent carriers require independent issued tokens");
                var targets = List.of(SpellTarget.origin(source));
                first.resume(level, targets);
                TagValueOutput checkpoint = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
                first.save(checkpoint);
                var restored = new CarrierCharge();
                var copied = new CarrierCharge();
                restored.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), checkpoint.buildResult()));
                copied.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), checkpoint.buildResult()));
                restored.resume(level, targets);
                copied.resume(level, targets);
                copied.resume(level, targets);
                helper.assertTrue(copied.isSpent() && emissions.get() == 2, "A copied saved payload must not catch up and emit after its replay is rejected");
                second.resume(level, targets);
                restored.resume(level, targets);
                helper.assertTrue(emissions.get() == 4, "Independent branches and legitimate recurring carrier emissions must remain usable");
                SpellAuthorities.revoke(owner, authority);
                restored.resume(level, targets);
                helper.assertTrue(restored.isSpent() && emissions.get() == 4, "Revoked carrier authority must stop subsequent emissions");
                helper.succeed();
            } finally {
                NeoForge.EVENT_BUS.unregister(listener);
                source.discard();
                level.getServer().getPlayerList().remove(owner);
            }
        });
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
