package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellRayTrace;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public record ChainDelivery(double range, double jumpRange, float falloff, float width) implements SpellBehavior {
    public static final MapCodec<ChainDelivery> CODEC = RecordCodecBuilder.mapCodec(i -> i
            .group(Codec.doubleRange(1.0, 64.0).optionalFieldOf("range", 16.0).forGetter(ChainDelivery::range),
                    Codec.doubleRange(1.0, 32.0).optionalFieldOf("jump_range", 6.0).forGetter(ChainDelivery::jumpRange),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("falloff", 0.8F).forGetter(ChainDelivery::falloff), Codec.FLOAT.optionalFieldOf("width", 0.5F).forGetter(ChainDelivery::width))
            .apply(i, ChainDelivery::new));

    private static final String JUMPS = "jumps";

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.CHAIN.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        SpellLook look = SpellLook.downstream(ctx);
        int jumps = Math.max(0, ctx.setting(JUMPS));
        for (SpellTarget origin : incoming) {
            Entity source = origin.entity().orElse(ctx.caster());
            HitResult first = SpellRayTrace.trace(ctx.level(), source, origin.position(), origin.direction(), range);
            Vec3 end = SpellRayTrace.endOf(first, origin.position(), origin.direction(), range);
            ctx.fx().arc(origin.position(), end, look.color(), width * ctx.power());
            if (first.getType() == HitResult.Type.MISS) {
                continue;
            }
            SpellState state = ctx.state();
            ctx.proceed(List.of(SpellTarget.of(first, origin.direction())), state);
            if (!(first instanceof EntityHitResult entityHit)) {
                continue;
            }
            hop(ctx, look, entityHit.getEntity(), source, jumps, state);
        }
    }

    private void hop(CastContext ctx, SpellLook look, Entity start, Entity source, int jumps, SpellState state) {
        Set<Integer> struck = new HashSet<>();
        struck.add(start.getId());
        Entity current = start;
        SpellState hopState = state;
        for (int jump = 0; jump < jumps; jump++) {
            Vec3 from = current.getBoundingBox().getCenter();
            Vec3 eye = current.getEyePosition();
            Entity previous = current;
            Optional<LivingEntity> next = SpellTargeting.nearest(ctx.level(), from, jumpRange,
                    living -> living != source && !struck.contains(living.getId()) && !ctx.isAlly(living) && SpellTargeting.canSee(ctx.level(), eye, living, previous));
            if (next.isEmpty()) {
                return;
            }
            LivingEntity target = next.get();
            struck.add(target.getId());
            hopState = hopState.multiply(SpellStats.POWER, falloff);
            Vec3 to = target.getBoundingBox().getCenter();
            ctx.fx().arc(from, to, look.color(), width * hopState.power());
            ctx.proceed(List.of(SpellTarget.entity(target, to.subtract(from))), hopState);
            current = target;
        }
    }
}
