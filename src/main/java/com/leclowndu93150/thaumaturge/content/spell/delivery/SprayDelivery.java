package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.world.Cones;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellRayTrace;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public record SprayDelivery(float angle, int maxTargets, int puffs) implements SpellBehavior {
    public static final MapCodec<SprayDelivery> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.floatRange(5.0F, 180.0F)
                            .optionalFieldOf("angle", 60.0F)
                            .forGetter(SprayDelivery::angle),
                    Codec.intRange(1, 32).optionalFieldOf("max_targets", 8).forGetter(SprayDelivery::maxTargets),
                    Codec.intRange(0, 16).optionalFieldOf("puffs", 6).forGetter(SprayDelivery::puffs))
            .apply(i, SprayDelivery::new));

    private static final String RANGE = "range";
    private static final double PUFF_SPEED = 0.35;

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.SPRAY.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        double range = Math.max(1, ctx.setting(RANGE));
        float halfAngle = angle * Mth.DEG_TO_RAD / 2.0F;
        double halfCos = Math.cos(halfAngle);
        SpellLook look = SpellLook.downstream(ctx);
        List<SpellTarget> out = new ArrayList<>();
        for (SpellTarget origin : incoming) {
            Entity source = origin.entity().orElse(ctx.caster());
            Vec3 apex = origin.position();
            Vec3 axis = origin.direction();
            for (int puff = 0; puff < puffs; puff++) {
                ctx.fx()
                        .burst(
                                look.fx(),
                                apex,
                                Cones.jitter(axis, halfAngle, ctx.random()).scale(PUFF_SPEED * range / 3.0),
                                look.color());
            }
            List<LivingEntity> caught = SpellTargeting.livingWithin(
                    ctx.level(),
                    apex,
                    range,
                    living -> living != source
                            && SpellTargeting.withinCone(apex, axis, living, range, halfCos)
                            && SpellTargeting.canSee(ctx.level(), apex, living, source));
            caught.sort(Comparator.comparingDouble(living -> living.distanceToSqr(apex)));
            for (int index = 0; index < Math.min(maxTargets, caught.size()); index++) {
                LivingEntity living = caught.get(index);
                out.add(SpellTarget.entity(
                        living, living.getBoundingBox().getCenter().subtract(apex)));
            }
            HitResult ground = SpellRayTrace.clipBlocks(ctx.level(), source, apex, apex.add(axis.scale(range)));
            if (ground.getType() == HitResult.Type.BLOCK) {
                out.add(SpellTarget.of(ground, axis));
            }
        }
        ctx.proceed(out);
    }
}
