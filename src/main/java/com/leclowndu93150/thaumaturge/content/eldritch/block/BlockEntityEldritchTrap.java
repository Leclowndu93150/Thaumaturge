package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.warp.WarpManager;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class BlockEntityEldritchTrap extends BlockEntity {
    private static final double TRIGGER_RANGE = 3.0;
    private static final float ZAP_DAMAGE = 2.0F;
    private static final int COOLDOWN_BASE = 10;
    private static final int COOLDOWN_SPREAD = 25;

    private int count = 20;

    public BlockEntityEldritchTrap(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_TRAP.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos) {
        if (count-- > 0) {
            return;
        }
        count = COOLDOWN_BASE + level.getRandom().nextInt(COOLDOWN_SPREAD);
        Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TRIGGER_RANGE, false);
        if (player == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        player.hurtServer(serverLevel, serverLevel.damageSources().magic(), ZAP_DAMAGE);
        if (level.getRandom().nextBoolean() && player instanceof ServerPlayer serverPlayer) {
            WarpManager.addWarp(serverPlayer, 1 + level.getRandom().nextInt(2), WarpType.TEMPORARY);
        }
        Effects.arcBolt(serverLevel, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)).to(new Vec3(player.getX(), player.getEyeY(), player.getZ())).send();
    }
}
