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
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record PlaceBlockAction(BlockState state, float chance, boolean needsFloor) implements SpellAction {
    public static final MapCodec<PlaceBlockAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    BlockState.CODEC.fieldOf("state").forGetter(PlaceBlockAction::state),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(PlaceBlockAction::chance),
                    Codec.BOOL.optionalFieldOf("needs_floor", true).forGetter(PlaceBlockAction::needsFloor))
            .apply(i, PlaceBlockAction::new));

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.PLACE_BLOCK.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        if (ctx.target().block().isEmpty()) {
            return;
        }
        ServerLevel level = ctx.cast().level();
        BlockPos centre = ActionTargets.facing(ctx);
        for (BlockPos pos : ActionTargets.around(ctx, centre, ctx.radius())) {
            if (!level.getBlockState(pos).isAir() || !state.canSurvive(level, pos)) {
                continue;
            }
            if (needsFloor && !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
                continue;
            }
            if (ctx.cast().random().nextFloat() < chance && SpellBlockAccess.mayPlace(ctx.cast(), pos)) {
                level.setBlock(pos, state, Block.UPDATE_ALL);
            }
        }
    }
}
