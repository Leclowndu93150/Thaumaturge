package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;

public final class SpellBlockAccess {
    private SpellBlockAccess() {}

    public static boolean mayBreak(CastContext ctx, BlockPos pos) {
        ServerLevel level = ctx.level();
        LivingEntity caster = ctx.caster();
        if (caster instanceof ServerPlayer player) {
            return player.mayInteract(level, pos)
                    && !CommonHooks.fireBlockBreak(
                                    level,
                                    player.gameMode.getGameModeForPlayer(),
                                    player,
                                    pos,
                                    level.getBlockState(pos))
                            .isCanceled();
        }
        return mayGrief(ctx, caster);
    }

    public static boolean mayPlace(CastContext ctx, BlockPos pos) {
        ServerLevel level = ctx.level();
        LivingEntity caster = ctx.caster();
        if (caster instanceof ServerPlayer player) {
            return player.mayInteract(level, pos)
                    && !EventHooks.onBlockPlace(
                            player, BlockSnapshot.create(level.dimension(), level, pos), Direction.UP);
        }
        return mayGrief(ctx, caster);
    }

    private static boolean mayGrief(CastContext ctx, LivingEntity caster) {
        if (caster == null && ctx.casterId().isPresent()) {
            return false;
        }
        return EventHooks.canEntityGrief(ctx.level(), caster);
    }
}
