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
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public record FreezeWaterAction(float base, float perPower, float max) implements SpellAction {
    public static final MapCodec<FreezeWaterAction> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.FLOAT.optionalFieldOf("base", 0.0F).forGetter(FreezeWaterAction::base), Codec.FLOAT.optionalFieldOf("per_power", 1.0F).forGetter(FreezeWaterAction::perPower),
                    Codec.FLOAT.optionalFieldOf("max", 6.0F).forGetter(FreezeWaterAction::max)).apply(i, FreezeWaterAction::new));

    private static final int MELT_MIN_TICKS = 60;
    private static final int MELT_MAX_TICKS = 120;

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.FREEZE_WATER.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        if (ctx.target().block().isEmpty()) {
            return;
        }
        ServerLevel level = ctx.cast().level();
        BlockState ice = Blocks.FROSTED_ICE.defaultBlockState();
        float radius = Math.min(max, ActionTargets.magnitude(base, perPower, ctx) + ctx.radius());
        for (BlockPos pos : ActionTargets.around(ctx, ActionTargets.facing(ctx), radius)) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.WATER) && state.getFluidState().isSource() && level.isUnobstructed(ice, pos, CollisionContext.empty())) {
                level.setBlockAndUpdate(pos, ice);
                level.scheduleTick(pos, Blocks.FROSTED_ICE, Mth.nextInt(level.getRandom(), MELT_MIN_TICKS, MELT_MAX_TICKS));
            }
        }
    }
}
