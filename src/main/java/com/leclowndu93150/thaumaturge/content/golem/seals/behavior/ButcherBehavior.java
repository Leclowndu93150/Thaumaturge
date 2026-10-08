package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

public final class ButcherBehavior extends HuntBehavior {
    private static final int STAGGER = 200;
    private static final int SCAN_PERIOD = 200;
    private static final int HERD_SIZE = 3;

    private final SealClock clock = new SealClock(STAGGER);
    private boolean hunting;

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.advance() % SCAN_PERIOD != 0 || hunting) {
            return;
        }
        AABB pasture = SealArea.bounds(seal);
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, pasture)) {
            if (isLivestock(candidate) && herdSize(level, pasture, candidate) >= HERD_SIZE) {
                mark(level, seal, candidate);
                hunting = true;
                return;
            }
        }
    }

    private static int herdSize(ServerLevel level, AABB pasture, LivingEntity member) {
        int count = 0;
        for (LivingEntity kin : level.getEntitiesOfClass(member.getClass(), pasture)) {
            if (isLivestock(kin) && ++count >= HERD_SIZE) {
                break;
            }
        }
        return count;
    }

    private static boolean isLivestock(LivingEntity entity) {
        if (!(entity instanceof Animal || entity instanceof WaterAnimal)
                || entity instanceof Enemy
                || entity instanceof AbstractGolem) {
            return false;
        }
        return !(entity instanceof TamableAnimal pet && pet.isTame()) && !entity.isBaby();
    }

    @Override
    protected boolean isQuarry(ServerLevel level, ISealEntity seal, LivingEntity target) {
        return isLivestock(target);
    }

    @Override
    protected void onHuntOver() {
        hunting = false;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        return true;
    }
}
