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

public final class FellBehavior extends CellWorkBehavior {
    private static final int STAGGER = 33;

    public FellBehavior() {
        super(STAGGER);
    }

    @Override
    protected boolean isWorkable(Level level, ISealEntity seal, BlockPos pos) {
        return level.getBlockState(pos).is(BlockTags.LOGS);
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        if (stillMine(level, seal, task)) {
            golem.swingArm();
            if (EnchantMining.breakFurthest(
                    level,
                    task.pos(),
                    level.getBlockState(task.pos()),
                    TTFakePlayer.GOLEM.at(level, golem.asEntity()))) {
                keepAlive(task);
                golem.addRankXp(1);
                return false;
            }
            release(task);
        }
        task.end();
        return true;
    }
}
