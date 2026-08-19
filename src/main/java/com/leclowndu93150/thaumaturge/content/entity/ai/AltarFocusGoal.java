package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultistCleric;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

public final class AltarFocusGoal extends Goal {
    private static final int CHECK_INTERVAL_TICKS = 40;
    private static final double MAX_DISTANCE_SQ = 16.0;

    private final EntityCultistCleric cleric;

    public AltarFocusGoal(EntityCultistCleric cleric) {
        this.cleric = cleric;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return cleric.isRitualist() && cleric.hasHome();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (cleric.tickCount % CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        BlockPos home = cleric.getHomePosition();
        if (home.distSqr(cleric.blockPosition()) > MAX_DISTANCE_SQ || !cleric.level().getBlockState(home).is(TCBlocks.ELDRITCH_ALTAR.get())) {
            cleric.setRitualist(false);
        }
    }
}
