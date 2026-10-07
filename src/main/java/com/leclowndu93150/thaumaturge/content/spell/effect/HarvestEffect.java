package com.leclowndu93150.thaumaturge.content.spell.effect;

import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class HarvestEffect extends AbstractEffectBehavior {
    public static final MapCodec<HarvestEffect> CODEC = MapCodec.unit(new HarvestEffect());

    private static final String RADIUS = "radius";
    private static final int MAX_RADIUS = 6;

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.HARVEST.get();
    }

    @Override
    protected boolean widens() {
        return false;
    }

    @Override
    protected void apply(CastContext ctx, SpellTarget target, float power, int index) {
        if (target.block().isEmpty()) {
            return;
        }
        ServerLevel level = ctx.level();
        BlockPos centre = target.block().get().getBlockPos();
        int radius = Math.min(MAX_RADIUS, ctx.setting(RADIUS) + Math.round(ctx.state().get(SpellStats.RADIUS)));
        boolean reaped = false;
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-radius, -1, -radius), centre.offset(radius, 1, radius))) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state) && ctx.budget().claim(pos)) {
                reap(ctx, level, pos.immutable(), state, crop);
                reaped = true;
            }
        }
        if (reaped) {
            ctx.fx().impact(ctx.part().fx(), Vec3.atCenterOf(centre), ctx.color());
        }
    }

    private static void reap(CastContext ctx, ServerLevel level, BlockPos pos, BlockState state, CropBlock crop) {
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, ctx.caster(), ItemStack.EMPTY);
        Item seed = crop.asItem();
        boolean replanted = false;
        for (ItemStack drop : drops) {
            if (!replanted && drop.is(seed)) {
                drop.shrink(1);
                replanted = true;
            }
            if (!drop.isEmpty()) {
                Block.popResource(level, pos, drop);
            }
        }
        level.setBlockAndUpdate(pos, replanted ? crop.getStateForAge(0) : state.getFluidState().createLegacyBlock());
    }
}
