package com.leclowndu93150.thaumaturge.content.spell.effect;

import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class AspectBurstEffect extends AbstractEffectBehavior {
    public static final MapCodec<AspectBurstEffect> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.FLOAT.optionalFieldOf("default_duration", 2.0F).forGetter(AspectBurstEffect::defaultDuration),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("falloff", 0.75F).forGetter(AspectBurstEffect::falloff)).apply(i, AspectBurstEffect::new));

    private static final String RADIUS = "radius";
    private static final float MAX_RADIUS = 8.0F;
    private static final int RING_FX_PER_BLOCK = 3;
    private static final double RING_LIFT = 0.25;

    private final float defaultDuration;
    private final float falloff;

    public AspectBurstEffect(float defaultDuration, float falloff) {
        this.defaultDuration = defaultDuration;
        this.falloff = falloff;
    }

    public float defaultDuration() {
        return defaultDuration;
    }

    public float falloff() {
        return falloff;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.ASPECT_BURST.get();
    }

    @Override
    protected boolean widens() {
        return false;
    }

    @Override
    protected void apply(CastContext ctx, SpellTarget target, float power, int index) {
        Optional<AspectAffinity> found = AffinityRunner.affinity(ctx);
        if (found.isEmpty()) {
            return;
        }
        AspectAffinity affinity = found.get();
        float radius = Math.min(MAX_RADIUS, ctx.setting(RADIUS) + ctx.state().get(SpellStats.RADIUS));
        float magnitude = AffinityRunner.magnitude(ctx, power);
        float duration = AffinityRunner.duration(ctx, defaultDuration);
        Vec3 centre = target.position();
        ring(ctx, affinity, centre, radius);
        Entity struck = target.entity().orElse(null);
        if (struck != null) {
            AffinityRunner.strike(ctx, affinity, target, magnitude, duration, radius);
        }
        for (LivingEntity near : SpellTargeting.livingWithin(ctx.level(), centre, radius, living -> living != struck && living != ctx.caster())) {
            if (ctx.budget().claim(near)) {
                SpellTarget splash = SpellTarget.entity(near, near.getBoundingBox().getCenter().subtract(centre));
                AffinityRunner.strike(ctx, affinity, splash, magnitude * falloff, duration * falloff, radius);
            }
        }
        BlockHitResult ground = target.block().orElseGet(() -> new BlockHitResult(centre, Direction.UP, BlockPos.containing(centre).below(), false));
        AffinityRunner.strike(ctx, affinity, new SpellTarget(ground, centre, target.direction()), magnitude, duration, radius);
    }

    private static void ring(CastContext ctx, AspectAffinity affinity, Vec3 centre, float radius) {
        ctx.fx().impact(affinity.fx(), centre, ctx.color());
        int points = Math.max(4, Math.round(radius * RING_FX_PER_BLOCK));
        for (int point = 0; point < points; point++) {
            double angle = Math.PI * 2.0 * point / points;
            Vec3 at = centre.add(Math.cos(angle) * radius, RING_LIFT, Math.sin(angle) * radius);
            ctx.fx().burst(affinity.fx(), at, at.subtract(centre).normalize().scale(0.1), ctx.color());
        }
    }
}
