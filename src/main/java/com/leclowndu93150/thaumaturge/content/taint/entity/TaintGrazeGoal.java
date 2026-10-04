package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public final class TaintGrazeGoal extends Goal {
    private static final int GRAZE_CHANCE = 250;
    private static final int GRAZE_TICKS = 40;
    private static final int BITE_TICK = 4;

    private final Sheep sheep;
    private int timer;

    public TaintGrazeGoal(Sheep sheep) {
        this.sheep = sheep;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!(sheep.level() instanceof ServerLevel level) || sheep.getRandom().nextInt(GRAZE_CHANCE) != 0) {
            return false;
        }
        BlockPos pos = sheep.blockPosition();
        return level.getBlockState(pos).is(Blocks.SHORT_GRASS)
                || level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK);
    }

    @Override
    public void start() {
        timer = GRAZE_TICKS;
        sheep.level().broadcastEntityEvent(sheep, EntityEvent.EAT_GRASS);
        sheep.getNavigation().stop();
    }

    @Override
    public void stop() {
        timer = 0;
    }

    @Override
    public boolean canContinueToUse() {
        return timer > 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        timer = Math.max(0, timer - 1);
        if (timer != BITE_TICK || !(sheep.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = sheep.blockPosition();
        int grassId = Block.getId(Blocks.GRASS_BLOCK.defaultBlockState());
        if (level.getBlockState(pos).is(Blocks.SHORT_GRASS)) {
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, grassId);
            level.destroyBlock(pos, false);
            infect(level, pos);
        } else if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) {
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos.below(), grassId);
            infect(level, pos);
        }
    }

    private void infect(ServerLevel level, BlockPos pos) {
        TaintBiomeManager.taintColumn(level, pos);
        BlockState here = level.getBlockState(pos);
        if (here.canBeReplaced() && here.getFluidState().isEmpty() && BlockTaintFibre.hasSolidAttachment(level, pos)) {
            level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
        }
        sheep.ate();
    }
}
