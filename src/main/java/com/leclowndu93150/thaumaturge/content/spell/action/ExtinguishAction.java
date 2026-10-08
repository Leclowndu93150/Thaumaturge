package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.content.spell.engine.SpellBlockAccess;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public record ExtinguishAction(float radius) implements SpellAction {
    public static final MapCodec<ExtinguishAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.floatRange(0.0F, 8.0F).optionalFieldOf("radius", 1.0F).forGetter(ExtinguishAction::radius))
            .apply(i, ExtinguishAction::new));

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.EXTINGUISH.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        ActionTargets.entity(ctx).ifPresent(entity -> entity.clearFire());
        if (ctx.target().block().isEmpty()) {
            return;
        }
        ServerLevel level = ctx.cast().level();
        boolean doused = false;
        for (BlockPos pos : ActionTargets.around(ctx, ActionTargets.facing(ctx), radius + ctx.radius())) {
            BlockState state = level.getBlockState(pos);
            if (!(state.is(BlockTags.FIRE) || state.getBlock() instanceof CampfireBlock)
                    || !SpellBlockAccess.mayBreak(ctx.cast(), pos)) {
                continue;
            }
            if (state.is(BlockTags.FIRE)) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                doused = true;
            } else if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false));
                doused = true;
            }
        }
        if (doused) {
            level.playSound(
                    null, ActionTargets.facing(ctx), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.0F);
        }
    }
}
