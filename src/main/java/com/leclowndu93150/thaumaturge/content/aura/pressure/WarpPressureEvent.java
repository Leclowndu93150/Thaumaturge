package com.leclowndu93150.thaumaturge.content.aura.pressure;

import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.warp.WarpManager;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;

public final class WarpPressureEvent extends AbstractFluxPressureEvent {
    private static final String NAME = "warp";
    private static final int WEIGHT = 5;
    private static final float COST = 20.0F;
    private static final boolean ALLOWED_NEAR_TAINT = true;
    private static final double RANGE = 16.0;
    private static final float NORMAL_WARP_CHANCE = 0.25F;
    private static final int NORMAL_WARP = 1;
    private static final int MIN_TEMPORARY_WARP = 2;
    private static final int TEMPORARY_WARP_SPREAD = 4;
    private static final String MESSAGE = "warp.thaumaturge.fluxevent.1";

    public WarpPressureEvent() {
        super(NAME, WEIGHT, COST, ALLOWED_NEAR_TAINT);
    }

    @Override
    public boolean fire(ServerLevel level, BlockPos origin, FluxPressureState state) {
        List<ServerPlayer> targets = level.getEntitiesOfClass(ServerPlayer.class, new AABB(origin).inflate(RANGE));
        if (targets.isEmpty()) {
            return false;
        }
        RandomSource random = level.getRandom();
        for (ServerPlayer player : targets) {
            WarpManager.sendActionBar(player, MESSAGE);
            if (random.nextFloat() < NORMAL_WARP_CHANCE) {
                WarpHelper.addWarp(player, NORMAL_WARP, WarpType.NORMAL);
            } else {
                WarpHelper.addWarp(
                        player, MIN_TEMPORARY_WARP + random.nextInt(TEMPORARY_WARP_SPREAD), WarpType.TEMPORARY);
            }
        }
        return true;
    }
}
