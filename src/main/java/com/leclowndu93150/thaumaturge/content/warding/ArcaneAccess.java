package com.leclowndu93150.thaumaturge.content.warding;

import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class ArcaneAccess {
    private ArcaneAccess() {}

    public static boolean canAccess(ServerLevel level, BlockPos pos, Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        return canAccess(level, pos, player.getUUID());
    }

    public static boolean canBind(ServerLevel level, BlockPos pos, Player player, boolean gold) {
        return gold ? player.getUUID().equals(owner(level, pos)) : canDelegateIron(level, pos, player.getUUID());
    }

    public static boolean canAccess(ServerLevel level, BlockPos pos, UUID player) {
        ArcaneLockChunkData locks = locks(level, pos);
        UUID owner = locks.owner(pos);
        return player.equals(owner) || (owner != null && locks.canAccess(pos, player));
    }

    public static boolean canDelegateIron(ServerLevel level, BlockPos pos, UUID player) {
        ArcaneLockChunkData locks = locks(level, pos);
        UUID owner = locks.owner(pos);
        return player.equals(owner) || (owner != null && locks.canDelegateIron(pos, player));
    }

    public static boolean grantAccess(ServerLevel level, BlockPos pos, UUID player, boolean gold) {
        ArcaneLockChunkData locks = locks(level, pos);
        if (locks.owner(pos) == null || !locks.grantAccess(pos, player, gold)) {
            return false;
        }
        level.getChunkAt(pos).setUnsaved(true);
        return true;
    }

    public static UUID owner(ServerLevel level, BlockPos pos) {
        return locks(level, pos).owner(pos);
    }

    public static void lock(ServerLevel level, BlockPos pos, UUID owner) {
        locks(level, pos).put(pos, owner);
        level.getChunkAt(pos).setUnsaved(true);
    }

    public static void removeLock(ServerLevel level, BlockPos pos) {
        locks(level, pos).remove(pos);
        level.getChunkAt(pos).setUnsaved(true);
    }

    public static boolean isLock(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(TCBlocks.ARCANE_DOOR.get())
                || level.getBlockState(pos).is(TCBlocks.ARCANE_PRESSURE_PLATE.get());
    }

    private static ArcaneLockChunkData locks(ServerLevel level, BlockPos pos) {
        return level.getChunkAt(pos).getData(TCAttachments.ARCANE_LOCKS.get());
    }
}
