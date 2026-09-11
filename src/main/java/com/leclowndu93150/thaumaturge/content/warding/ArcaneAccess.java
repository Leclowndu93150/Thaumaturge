package com.leclowndu93150.thaumaturge.content.warding;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class ArcaneAccess {
    private ArcaneAccess() {}

    public static boolean canAccess(ServerLevel level, BlockPos pos, Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        return WardHandler.canAccess(level, pos, player.getUUID());
    }

    public static boolean canBind(ServerLevel level, BlockPos pos, Player player, boolean gold) {
        return gold
                ? player.getUUID().equals(WardHandler.owner(level, pos))
                : WardHandler.canDelegateIron(level, pos, player.getUUID());
    }
}
