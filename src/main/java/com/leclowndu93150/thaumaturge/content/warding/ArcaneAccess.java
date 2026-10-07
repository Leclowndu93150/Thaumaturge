package com.leclowndu93150.thaumaturge.content.warding;

import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jspecify.annotations.Nullable;

public final class ArcaneAccess {
    private ArcaneAccess() {}

    public static boolean isLock(BlockState state) {
        return state.is(TTBlockTags.ARCANE_LOCKS);
    }

    public static BlockPos lockOrigin(BlockState state, BlockPos pos) {
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            return pos.below();
        }
        return pos;
    }

    public static boolean canAccess(ServerLevel level, BlockPos origin, Player player) {
        return player.getAbilities().instabuild || locks(level, origin).canAccess(origin, player.getUUID());
    }

    public static boolean sharesAccess(ServerLevel level, BlockPos first, BlockPos second) {
        ArcaneLockChunkData secondLocks = locks(level, second);
        for (UUID player : locks(level, first).accessors(first)) {
            if (secondLocks.canAccess(second, player)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canBind(ServerLevel level, BlockPos origin, Player player, boolean gold) {
        UUID id = player.getUUID();
        return gold ? id.equals(owner(level, origin)) : locks(level, origin).canDelegateIron(origin, id);
    }

    public static boolean grantAccess(ServerLevel level, BlockPos origin, UUID player, boolean gold) {
        if (!locks(level, origin).grantAccess(origin, player, gold)) {
            return false;
        }
        level.getChunkAt(origin).markUnsaved();
        return true;
    }

    public static @Nullable UUID owner(ServerLevel level, BlockPos origin) {
        return locks(level, origin).owner(origin);
    }

    public static void lock(ServerLevel level, BlockPos origin, UUID owner) {
        locks(level, origin).put(origin, owner);
        level.getChunkAt(origin).markUnsaved();
    }

    public static void removeLock(ServerLevel level, BlockPos origin) {
        if (locks(level, origin).remove(origin)) {
            level.getChunkAt(origin).markUnsaved();
        }
    }

    private static ArcaneLockChunkData locks(ServerLevel level, BlockPos pos) {
        return level.getChunkAt(pos).getData(TTAttachments.ARCANE_LOCKS.get());
    }
}
