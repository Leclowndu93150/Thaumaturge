package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellContinuation;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.event.SpellCastEvent;
import com.leclowndu93150.thaumaturge.api.spell.event.SpellNodeEvent;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

public final class SpellEngine {
    private static final int MAX_CAST_SOUNDS = 3;
    private static final float CAST_SOUND_VOLUME = 0.6F;
    private static final float CAST_SOUND_PITCH_SPREAD = 0.1F;

    private SpellEngine() {}

    public static SpellCastEvent.Pre firePre(
            LivingEntity caster, ItemStack focus, Spell spell, float power, float vis) {
        return NeoForge.EVENT_BUS.post(new SpellCastEvent.Pre(caster, focus, spell, power, vis));
    }

    public static void execute(
            LivingEntity caster, ItemStack focus, Spell spell, List<SpellTarget> origin, float power) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }
        CastRun run = new CastRun(level, UUID.randomUUID(), Optional.of(caster.getUUID()), caster);
        playCastSounds(level, caster, spell);
        runNode(run, spell.root(), origin, SpellState.DEFAULT.with(SpellStats.POWER, power));
        run.finish();
        NeoForge.EVENT_BUS.post(new SpellCastEvent.Post(caster, focus, spell));
    }

    public static void resume(ServerLevel level, SpellContinuation continuation, List<SpellTarget> targets) {
        if (continuation.isEmpty() || targets.isEmpty()) {
            return;
        }
        CastRun run = new CastRun(level, continuation.castId(), continuation.caster(), null);
        for (SpellNode node : continuation.nodes()) {
            runNode(run, node, targets, continuation.state());
        }
        run.finish();
    }

    static void runNode(CastRun run, SpellNode node, List<SpellTarget> targets, SpellState state) {
        if (!run.admitNode()) {
            return;
        }
        ServerLevel level = run.level();
        Optional<SpellPart> found = Spells.part(level.registryAccess(), node.part());
        if (found.isEmpty()) {
            return;
        }
        SpellPart part = found.get();
        Optional<Holder<IAspect>> aspect = part.aspect()
                .resolve(node.aspect(), level.registryAccess())
                .flatMap(key -> level.registryAccess()
                        .lookupOrThrow(IAspect.REGISTRY_KEY)
                        .get(key)
                        .map(holder -> (Holder<IAspect>) holder));
        NodeRun ctx = new NodeRun(
                run,
                node,
                part,
                state.multiply(SpellStats.POWER, part.power()),
                aspect,
                Spells.color(level.registryAccess(), node));
        SpellNodeEvent event = NeoForge.EVENT_BUS.post(new SpellNodeEvent(ctx, targets));
        if (event.isCanceled()) {
            return;
        }
        part.behavior().execute(ctx, event.targets());
    }

    private static void playCastSounds(ServerLevel level, LivingEntity caster, Spell spell) {
        Set<Holder<SoundEvent>> sounds = new LinkedHashSet<>();
        for (SpellNode node : spell.nodes()) {
            Spells.part(level.registryAccess(), node.part())
                    .flatMap(SpellPart::sound)
                    .ifPresent(sounds::add);
            Optional<ResourceKey<IAspect>> aspect = Spells.part(level.registryAccess(), node.part())
                    .flatMap(part -> part.aspect().resolve(node.aspect(), level.registryAccess()));
            aspect.flatMap(key -> Spells.affinity(level.registryAccess(), key))
                    .flatMap(affinity -> affinity.sound())
                    .ifPresent(sounds::add);
        }
        int played = 0;
        for (Holder<SoundEvent> sound : sounds) {
            if (played++ >= MAX_CAST_SOUNDS) {
                return;
            }
            level.playSound(
                    null,
                    caster.getX(),
                    caster.getEyeY(),
                    caster.getZ(),
                    sound,
                    SoundSource.PLAYERS,
                    CAST_SOUND_VOLUME,
                    1.0F + (level.getRandom().nextFloat() - 0.5F) * CAST_SOUND_PITCH_SPREAD);
        }
    }
}
