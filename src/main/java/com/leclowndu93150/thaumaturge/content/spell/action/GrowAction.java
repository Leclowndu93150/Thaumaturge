package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public record GrowAction(float base, float perPower) implements SpellAction {
    public static final MapCodec<GrowAction> CODEC = RecordCodecBuilder.mapCodec(i -> i
            .group(Codec.FLOAT.optionalFieldOf("base", 0.0F).forGetter(GrowAction::base), Codec.FLOAT.optionalFieldOf("per_power", 0.5F).forGetter(GrowAction::perPower)).apply(i, GrowAction::new));

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.GROW.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        if (ctx.target().block().isEmpty()) {
            return;
        }
        ServerLevel level = ctx.cast().level();
        int pulses = Math.max(1, Math.round(ActionTargets.magnitude(base, perPower, ctx)));
        for (BlockPos pos : ActionTargets.around(ctx, ctx.target().block().get().getBlockPos(), ctx.radius())) {
            for (int pulse = 0; pulse < pulses; pulse++) {
                BlockState state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof BonemealableBlock growable) || !growable.isValidBonemealTarget(level, pos, state)) {
                    break;
                }
                if (growable.isBonemealSuccess(level, level.getRandom(), pos, state)) {
                    growable.performBonemeal(level, level.getRandom(), pos, state);
                }
                level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 0);
            }
        }
    }
}
