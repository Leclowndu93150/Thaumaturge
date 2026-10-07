package com.leclowndu93150.thaumaturge.content.spell.effect;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class BreakEffect extends AbstractEffectBehavior {
    public static final MapCodec<BreakEffect> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.FLOAT.optionalFieldOf("vis_cost", 0.25F).forGetter(BreakEffect::visCost), Codec.FLOAT.optionalFieldOf("silk_vis", 0.25F).forGetter(BreakEffect::silkVis),
                    Codec.FLOAT.optionalFieldOf("fortune_vis", 0.1F).forGetter(BreakEffect::fortuneVis)).apply(i, BreakEffect::new));

    private static final String POWER = "power";
    private static final float HARDNESS_SCALE = 100.0F;
    private static final float STAGGER_DIVISOR = 3.0F;

    private final float visCost;
    private final float silkVis;
    private final float fortuneVis;

    public BreakEffect(float visCost, float silkVis, float fortuneVis) {
        this.visCost = visCost;
        this.silkVis = silkVis;
        this.fortuneVis = fortuneVis;
    }

    public float visCost() {
        return visCost;
    }

    public float silkVis() {
        return silkVis;
    }

    public float fortuneVis() {
        return fortuneVis;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.BREAK.get();
    }

    @Override
    protected boolean widens() {
        return false;
    }

    @Override
    protected void apply(CastContext ctx, SpellTarget target, float power, int index) {
        if (target.block().isEmpty() || !(ctx.caster() instanceof Player player)) {
            return;
        }
        ServerLevel level = ctx.level();
        BlockHitResult hit = target.block().get();
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        float hardness = state.getDestroySpeed(level, pos);
        if (state.isAir() || hardness < 0.0F) {
            return;
        }
        boolean silk = ctx.state().has(SpellStats.SILK_TOUCH);
        int fortune = Math.round(ctx.state().get(SpellStats.FORTUNE));
        float strength = Math.max(1, ctx.setting(POWER)) * power;
        float durability = (float) Math.sqrt(hardness * HARDNESS_SCALE);
        ctx.fx().impact(ctx.part().fx(), Vec3.atCenterOf(pos), ctx.color());
        BlockBreakerEngine.breaker(pos, state, player).showFx().silkTouch(silk).fortune(fortune).strength(strength).durability(durability)
                .delay((int) (durability / strength / STAGGER_DIVISOR * index)).visCost(visCost + (silk ? silkVis : 0.0F) + fortune * fortuneVis, TTAspects.PERDITIO).queue(level);
    }
}
