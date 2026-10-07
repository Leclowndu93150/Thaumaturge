package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public record AuraDelivery(int maxTargets) implements SpellBehavior {
    public static final MapCodec<AuraDelivery> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.intRange(1, 64).optionalFieldOf("max_targets", 16).forGetter(AuraDelivery::maxTargets)).apply(i, AuraDelivery::new));

    private static final String RADIUS = "radius";
    private static final int RING_POINTS = 12;
    private static final double RING_LIFT = 0.1;
    private static final double RING_SPEED = 0.08;

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.AURA.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        LivingEntity caster = ctx.caster();
        Vec3 centre = caster != null ? caster.position() : incoming.getFirst().position();
        double radius = Math.max(1, ctx.setting(RADIUS)) + ctx.state().get(SpellStats.RADIUS);
        SpellLook look = SpellLook.downstream(ctx);
        for (int point = 0; point < RING_POINTS; point++) {
            double angle = Math.PI * 2.0 * point / RING_POINTS;
            Vec3 heading = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            ctx.fx().burst(look.fx(), centre.add(0.0, RING_LIFT, 0.0), heading.scale(RING_SPEED * radius), look.color());
        }
        List<SpellTarget> out = new ArrayList<>();
        for (LivingEntity living : SpellTargeting.livingWithin(ctx.level(), centre, radius, living -> living != caster)) {
            if (out.size() >= maxTargets) {
                break;
            }
            Vec3 at = living.getBoundingBox().getCenter();
            out.add(SpellTarget.entity(living, at.subtract(centre)));
        }
        ctx.proceed(out);
    }
}
