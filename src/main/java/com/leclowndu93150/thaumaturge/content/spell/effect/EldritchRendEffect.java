package com.leclowndu93150.thaumaturge.content.spell.effect;

import com.leclowndu93150.thaumaturge.api.damagesource.TTDamageSources;
import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class EldritchRendEffect extends AbstractEffectBehavior {
    public static final MapCodec<EldritchRendEffect> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(Codec.DOUBLE.optionalFieldOf("knockback", 0.35).forGetter(EldritchRendEffect::knockback))
                    .apply(i, EldritchRendEffect::new));

    private static final int DUST_COLOR = 0xAC80D0;
    private static final float DUST_SCALE = 1.3F;
    private static final int DUST_COUNT = 12;
    private static final double DUST_SPREAD = 0.3;

    private final double knockback;

    public EldritchRendEffect(double knockback) {
        this.knockback = knockback;
    }

    public double knockback() {
        return knockback;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.ELDRITCH_REND.get();
    }

    @Override
    protected boolean widens() {
        return false;
    }

    @Override
    protected void apply(CastContext ctx, SpellTarget target, float power, int index) {
        if (!(target.entity().orElse(null) instanceof LivingEntity living)) {
            return;
        }
        ServerLevel level = ctx.level();
        if (living.hurt(TTDamageSources.eldritchSpell(level, ctx.caster()), power)) {
            Vec3 at = target.position();
            level.sendParticles(
                    new DustParticleOptions(Vec3.fromRGB24(DUST_COLOR).toVector3f(), DUST_SCALE),
                    at.x,
                    at.y,
                    at.z,
                    DUST_COUNT,
                    DUST_SPREAD,
                    DUST_SPREAD,
                    DUST_SPREAD,
                    0.0);
            living.knockback(knockback, -target.direction().x, -target.direction().z);
        }
    }
}
