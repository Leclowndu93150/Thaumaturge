package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public record SapAction(float base, float perPower, float max) implements SpellAction {
    public static final MapCodec<SapAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.FLOAT.optionalFieldOf("base", 0.0F).forGetter(SapAction::base),
            Codec.FLOAT.optionalFieldOf("per_power", 0.75F).forGetter(SapAction::perPower), Codec.FLOAT.optionalFieldOf("max", 5.0F).forGetter(SapAction::max)).apply(i, SapAction::new));

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.SAP.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        if (ctx.target().block().isEmpty()) {
            return;
        }
        ServerLevel level = ctx.cast().level();
        float radius = Math.min(max, ActionTargets.magnitude(base, perPower, ctx) + ctx.radius());
        for (BlockPos pos : ActionTargets.around(ctx, ctx.target().block().get().getBlockPos(), radius)) {
            BlockPos above = pos.above();
            if (level.getBlockState(above).isAir() && level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) {
                level.setBlockAndUpdate(above, TTBlocks.EFFECT_SAP.get().defaultBlockState());
            }
        }
    }
}
