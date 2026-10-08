package com.leclowndu93150.thaumaturge.content.spell.effect;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSplosion;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class PrimalEffect extends AbstractEffectBehavior {
    public static final MapCodec<PrimalEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.FLOAT.optionalFieldOf("damage", 4.0F).forGetter(PrimalEffect::damage),
                    Codec.FLOAT.optionalFieldOf("explosion", 1.5F).forGetter(PrimalEffect::explosion),
                    Codec.floatRange(0.0F, 1.0F)
                            .optionalFieldOf("chaos_chance", 0.01F)
                            .forGetter(PrimalEffect::chaosChance))
            .apply(i, PrimalEffect::new));

    private static final String POWER = "power";
    private static final float CHAOS_FLUX = 5.0F;
    private static final float CHAOS_TAINT_SPREAD = 6.0F;

    private final float damage;
    private final float explosion;
    private final float chaosChance;

    public PrimalEffect(float damage, float explosion, float chaosChance) {
        this.damage = damage;
        this.explosion = explosion;
        this.chaosChance = chaosChance;
    }

    public float damage() {
        return damage;
    }

    public float explosion() {
        return explosion;
    }

    public float chaosChance() {
        return chaosChance;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.PRIMAL.get();
    }

    @Override
    protected boolean widens() {
        return false;
    }

    @Override
    protected void apply(CastContext ctx, SpellTarget target, float power, int index) {
        ServerLevel level = ctx.level();
        Vec3 at = target.position();
        Effects.bamf(level, at).withSound().fancy().send();
        ctx.fx().impact(ctx.part().fx(), at, ctx.color());
        target.entity()
                .ifPresent(entity -> entity.hurt(
                        level.damageSources().indirectMagic(entity, ctx.caster()),
                        (damage + ctx.setting(POWER)) * power));
        level.explode(ctx.caster(), at.x, at.y, at.z, explosion, Level.ExplosionInteraction.MOB);
        if (ctx.random().nextFloat() < chaosChance) {
            unleash(level, BlockPos.containing(at), ctx);
        }
    }

    private static void unleash(ServerLevel level, BlockPos pos, CastContext ctx) {
        if (ctx.random().nextBoolean()) {
            NodeGenerator.createRandomNodeAt(
                    level,
                    pos.above(),
                    level.getRandom(),
                    false,
                    false,
                    true,
                    NodeGenerator.DEFAULT_SPECIAL_RARITY,
                    NodeGenerator.DEFAULT_BASE_AURA);
        } else if (ThaumaturgeCommonConfig.TAINT_FROM_FLUX.get() && !ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            TaintSplosion.burstOnSurface(level, pos, level.getRandom(), CHAOS_TAINT_SPREAD);
        } else {
            AuraHelper.polluteAura(level, pos, CHAOS_FLUX, true);
        }
    }
}
