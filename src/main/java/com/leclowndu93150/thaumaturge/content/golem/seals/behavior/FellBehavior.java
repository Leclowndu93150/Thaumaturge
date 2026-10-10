package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.equipment.EnchantMining;
import com.leclowndu93150.thaumaturge.server.TTFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class FellBehavior extends CellWorkBehavior {
    private static final int STAGGER = 59;
    private static final int CHOP_XP = 1;

    public FellBehavior() {
        super(STAGGER);
    }

    @Override
    protected boolean isWorkable(Level level, ISealEntity seal, BlockPos pos) {
        return level.getBlockState(pos).is(BlockTags.LOGS);
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        BlockPos stump = task.pos();
        BlockState state = level.getBlockState(stump);
        if (!state.is(BlockTags.LOGS)) {
            return retire(task);
        }
        golem.swingArm();
        if (!EnchantMining.breakFurthest(level, stump, state, TTFakePlayer.GOLEM.at(level, golem.asEntity()))) {
            return retire(task);
        }
        golem.addRankXp(CHOP_XP);
        keepAlive(task);
        return false;
    }
}
