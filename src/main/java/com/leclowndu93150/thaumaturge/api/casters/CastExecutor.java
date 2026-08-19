package com.leclowndu93150.thaumaturge.api.casters;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

final class CastExecutor {
    private CastExecutor() {}

    record StrikeKey(int entityId, UUID castId) {
    }

    static void run(Level level, FocusPackage pack, @Nullable LivingEntity caster, CastStreams streams, UUID castId, Set<StrikeKey> struck) {
        CastContext ctx = new CastContext(level, castId, pack.casterId().orElse(null), caster, pack.power(), pack.units());
        run(ctx, streams, struck);
    }

    private static void run(CastContext ctx, CastStreams streams, Set<StrikeKey> struck) {
        List<FocusUnit> units = ctx.program();
        for (int cursor = 0; cursor < units.size(); cursor++) {
            ctx.moveTo(cursor);
            FocusUnit unit = units.get(cursor);
            FocusElement element = FocusEngine.element(unit.element());
            if (element == null) {
                return;
            }
            FocusSettings settings = FocusSettings.of(element, unit.settings());
            ctx.foldPower(element.powerMultiplier(settings));
            switch (element) {
                case FocusSplit split -> {
                    CastStreams branchInput = split.branchStreams(ctx, settings, streams);
                    for (FocusPackage branch : unit.branches()) {
                        run(ctx.branch(branch.power() * ctx.power(), branch.units()), branchInput, struck);
                    }
                    return;
                }
                case FocusMod mod -> streams = mod.modify(ctx, settings, streams);
                case FocusMedium medium -> {
                    CastStreams out = medium.cast(ctx, settings, streams);
                    if (out == null) {
                        return;
                    }
                    streams = out;
                }
                case FocusEffect effect -> streams = applyEffect(ctx, effect, settings, streams, struck);
            }
        }
    }

    private static CastStreams applyEffect(CastContext ctx, FocusEffect effect, FocusSettings settings, CastStreams streams, Set<StrikeKey> struck) {
        HitResult[] targets = streams.targets();
        if (targets == null) {
            return CastStreams.EMPTY;
        }
        Trajectory[] trajectories = streams.trajectories();
        for (int i = 0; i < targets.length; i++) {
            HitResult target = targets[i];
            if (target instanceof EntityHitResult entityHit) {
                Entity victim = entityHit.getEntity();
                if (!struck.add(new StrikeKey(victim.getId(), ctx.castId())) && victim.invulnerableTime > 0) {
                    victim.invulnerableTime = 0;
                }
            }
            Trajectory trajectory = trajectories != null ? (trajectories.length == targets.length ? trajectories[i] : trajectories[0]) : null;
            effect.apply(ctx, settings, target, trajectory, i);
        }
        return CastStreams.EMPTY;
    }
}
