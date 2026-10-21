package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

public final class SpellBlockAccess {
    private SpellBlockAccess() {}

    public static boolean mayAffect(CastContext ctx, BlockPos pos) {
        return ctx.level().hasChunkAt(pos) && ctx.level().isInWorldBounds(pos) && ctx.permits(SpellTarget.of(new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false), Vec3.ZERO));
    }

    public static boolean mayBreak(CastContext ctx, BlockPos pos) {
        if (!mayAffect(ctx, pos)) {
            return false;
        }
        ServerLevel level = ctx.level();
        ServerPlayer player = actingPlayer(ctx);
        if (player != null) {
            return player.mayInteract(level, pos) && !CommonHooks.fireBlockBreak(level, player.gameMode.getGameModeForPlayer(), player, pos, level.getBlockState(pos)).isCanceled();
        }
        return ctx.authority().isPresent() || mayGrief(ctx, ctx.caster());
    }

    public static boolean mayPlace(CastContext ctx, BlockPos pos) {
        if (!mayAffect(ctx, pos)) {
            return false;
        }
        ServerLevel level = ctx.level();
        ServerPlayer player = actingPlayer(ctx);
        if (player != null) {
            return player.mayInteract(level, pos) && !EventHooks.onBlockPlace(player, BlockSnapshot.create(level.dimension(), level, pos), Direction.UP);
        }
        return ctx.authority().isPresent() || mayGrief(ctx, ctx.caster());
    }

    private static @Nullable ServerPlayer actingPlayer(CastContext ctx) {
        if (ctx.authority().isPresent()) {
            return ctx.level().getServer().getPlayerList().getPlayer(ctx.authority().get().ownerId());
        }
        return ctx.caster() instanceof ServerPlayer player ? player : null;
    }

    private static boolean mayGrief(CastContext ctx, @Nullable LivingEntity caster) {
        if (caster == null && ctx.casterId().isPresent()) {
            return false;
        }
        return EventHooks.canEntityGrief(ctx.level(), caster);
    }
}
