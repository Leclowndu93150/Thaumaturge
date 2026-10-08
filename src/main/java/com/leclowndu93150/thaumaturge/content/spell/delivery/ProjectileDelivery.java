package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.carrier.CarrierPayload;
import com.leclowndu93150.thaumaturge.content.spell.carrier.SpellProjectile;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

public record ProjectileDelivery(float speed, float speedPerStep, double gravity, float splash)
        implements SpellBehavior {
    public static final MapCodec<ProjectileDelivery> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.floatRange(0.05F, 5.0F)
                            .optionalFieldOf("speed", 0.66F)
                            .forGetter(ProjectileDelivery::speed),
                    Codec.floatRange(0.0F, 2.0F)
                            .optionalFieldOf("speed_per_step", 0.33F)
                            .forGetter(ProjectileDelivery::speedPerStep),
                    Codec.doubleRange(0.0, 0.2).optionalFieldOf("gravity", 0.01).forGetter(ProjectileDelivery::gravity),
                    Codec.floatRange(0.0F, 8.0F).optionalFieldOf("splash", 0.0F).forGetter(ProjectileDelivery::splash))
            .apply(i, ProjectileDelivery::new));

    private static final String SPEED = "speed";

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.PROJECTILE.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        LivingEntity caster = ctx.caster();
        if (caster == null || !ctx.hasNext()) {
            return;
        }
        float steps = ctx.part()
                .setting(SPEED)
                .map(spec -> (float) spec.steps(ctx.setting(SPEED)))
                .orElse(0.0F);
        float velocity = (speed + speedPerStep * steps) * ctx.state().get(SpellStats.SPEED);
        CarrierPayload payload = new CarrierPayload(ctx.continuation(), SpellLook.downstream(ctx));
        for (SpellTarget origin : incoming) {
            SpellProjectile.launch(ctx.level(), caster, payload, origin, velocity, gravity, splash);
        }
    }

    @Override
    public boolean standalone() {
        return false;
    }
}
