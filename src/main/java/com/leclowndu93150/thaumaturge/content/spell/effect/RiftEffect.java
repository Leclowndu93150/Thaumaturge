package com.leclowndu93150.thaumaturge.content.spell.effect;

import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.block.PortableHoles;
import com.leclowndu93150.thaumaturge.content.spell.engine.SpellBlockAccess;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.BlockHitResult;

public final class RiftEffect extends AbstractEffectBehavior {
    public static final MapCodec<RiftEffect> CODEC = MapCodec.unit(new RiftEffect());

    private static final String DEPTH = "depth";
    private static final String DURATION = "duration";
    private static final int TICKS_PER_SECOND = 20;

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.RIFT.get();
    }

    @Override
    protected boolean widens() {
        return false;
    }

    private static boolean mayOpenRing(CastContext ctx, BlockPos centre, Direction.Axis axis) {
        for (BlockPos pos : BlockPos.betweenClosed(ringCorner(centre, axis, -1), ringCorner(centre, axis, 1))) {
            if (PortableHoles.canOpen(ctx.level(), pos) && !SpellBlockAccess.mayBreak(ctx, pos)) {
                return false;
            }
        }
        return true;
    }

    private static BlockPos ringCorner(BlockPos centre, Direction.Axis axis, int offset) {
        return centre.offset(
                axis == Direction.Axis.X ? 0 : offset,
                axis == Direction.Axis.Y ? 0 : offset,
                axis == Direction.Axis.Z ? 0 : offset);
    }

    @Override
    protected void apply(CastContext ctx, SpellTarget target, float power, int index) {
        if (target.block().isEmpty()) {
            return;
        }
        ServerLevel level = ctx.level();
        BlockHitResult hit = target.block().get();
        Direction face = hit.getDirection();
        int reach = Math.max(1, Math.round(ctx.setting(DEPTH) * power));
        int ticks = Math.round(
                ctx.setting(DURATION) * TICKS_PER_SECOND * ctx.state().get(SpellStats.DURATION));
        int depth = 0;
        BlockPos cursor = hit.getBlockPos();
        while (depth < reach && PortableHoles.canOpen(level, cursor) && mayOpenRing(ctx, cursor, face.getAxis())) {
            depth++;
            cursor = cursor.relative(face.getOpposite());
        }
        if (depth > 0) {
            ctx.fx().impact(ctx.part().fx(), target.position(), ctx.color());
            PortableHoles.open(level, hit.getBlockPos(), face, depth, ticks);
        }
    }
}
