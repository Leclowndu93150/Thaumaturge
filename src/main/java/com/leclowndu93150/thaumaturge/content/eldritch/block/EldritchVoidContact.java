package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class EldritchVoidContact {
    private static final int GRACE_TICKS = 20;

    private EldritchVoidContact() {}

    public static void touch(Level level, Entity entity) {
        if (!(level instanceof ServerLevel serverLevel)
                || entity.tickCount <= GRACE_TICKS
                || entity instanceof Player player && player.getAbilities().invulnerable) {
            return;
        }
        entity.hurt(
                serverLevel.damageSources().fellOutOfWorld(),
                ThaumaturgeServerConfig.LABYRINTH.voidContactDamage.get().floatValue());
    }

    public static boolean isEnclosed(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            cursor.setWithOffset(pos, dir);
            BlockState neighbor = level.getBlockState(cursor);
            if (!neighbor.is(TTBlocks.ELDRITCH_NOTHING.get()) && !neighbor.isCollisionShapeFullBlock(level, cursor)) {
                return false;
            }
        }
        return true;
    }

    public static BlockState exposedState() {
        return TTBlocks.ELDRITCH_NOTHING.get().defaultBlockState().setValue(BlockEldritchNothing.EXPOSED, Boolean.TRUE);
    }

    public static BlockState dormantState() {
        return TTBlocks.ELDRITCH_NOTHING_DORMANT.get().defaultBlockState();
    }
}
