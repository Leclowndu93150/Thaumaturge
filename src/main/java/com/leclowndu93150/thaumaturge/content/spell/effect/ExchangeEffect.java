package com.leclowndu93150.thaumaturge.content.spell.effect;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.api.casters.IFocusBlockPicker;
import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumaturge.content.spell.casting.CasterHands;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class ExchangeEffect extends AbstractEffectBehavior implements IFocusBlockPicker {
    public static final MapCodec<ExchangeEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.FLOAT.optionalFieldOf("vis_cost", 0.25F).forGetter(ExchangeEffect::visCost),
                    Codec.FLOAT.optionalFieldOf("silk_vis", 0.25F).forGetter(ExchangeEffect::silkVis),
                    Codec.FLOAT.optionalFieldOf("fortune_vis", 0.1F).forGetter(ExchangeEffect::fortuneVis))
            .apply(i, ExchangeEffect::new));

    private static final int SWAP_FX_COLOR = 0x7AA621;

    private final float visCost;
    private final float silkVis;
    private final float fortuneVis;

    public ExchangeEffect(float visCost, float silkVis, float fortuneVis) {
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
        return TTSpellBehaviors.EXCHANGE.get();
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
        ItemStack casterStack = CasterHands.held(player);
        if (!(casterStack.getItem() instanceof ICaster caster)) {
            return;
        }
        BlockState picked = caster.getPickedBlock(casterStack);
        if (picked == null || picked.isAir()) {
            return;
        }
        BlockPos pos = target.block().get().getBlockPos();
        boolean silk = ctx.state().has(SpellStats.SILK_TOUCH);
        int fortune = Math.round(ctx.state().get(SpellStats.FORTUNE));
        ctx.fx().impact(ctx.part().fx(), Vec3.atCenterOf(pos), ctx.color());
        BlockBreakerEngine.swapper(pos, ctx.level().getBlockState(pos), picked, player)
                .consumeTarget()
                .showFx(SWAP_FX_COLOR, false)
                .pickupDrops()
                .silkTouch(silk)
                .fortune(fortune)
                .visCost(visCost + (silk ? silkVis : 0.0F) + fortune * fortuneVis, TTAspects.PERMUTATIO)
                .queue(ctx.level());
    }
}
