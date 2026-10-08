package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public record ErodeAction(float base, float perPower, float visCost) implements SpellAction {
    public static final MapCodec<ErodeAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.FLOAT.optionalFieldOf("base", 0.0F).forGetter(ErodeAction::base),
                    Codec.FLOAT.optionalFieldOf("per_power", 0.1F).forGetter(ErodeAction::perPower),
                    Codec.FLOAT.optionalFieldOf("vis_cost", 0.1F).forGetter(ErodeAction::visCost))
            .apply(i, ErodeAction::new));

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.ERODE.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        if (ctx.target().block().isEmpty() || !(ctx.cast().caster() instanceof Player player)) {
            return;
        }
        ServerLevel level = ctx.cast().level();
        float hardness = ActionTargets.magnitude(base, perPower, ctx);
        int delay = 0;
        for (BlockPos pos : ActionTargets.around(ctx, ctx.target().block().get().getBlockPos(), ctx.radius())) {
            BlockState state = level.getBlockState(pos);
            float destroy = state.getDestroySpeed(level, pos);
            if (!state.isAir() && destroy >= 0.0F && destroy <= hardness) {
                BlockBreakerEngine.breaker(pos, state, player)
                        .delay(delay++)
                        .visCost(visCost, ctx.aspect().unwrapKey().orElseThrow())
                        .queue(level);
            }
        }
    }
}
