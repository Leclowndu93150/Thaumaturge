package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISeal;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigArea;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealGui;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandler;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class SealButcher implements ISeal, ISealGui, ISealConfigArea {
    private static final int SCAN_INTERVAL = 200;
    private static final int MIN_HERD_SIZE = 3;
    private static final short TASK_LIFESPAN = 10;

    private int delay = System.identityHashCode(this) % 200;
    private boolean wait;

    @Override
    public Identifier getKey() {
        return TCIds.rl("butcher");
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay++ % SCAN_INTERVAL != 0 || wait) {
            return;
        }
        AABB area = GolemHelper.getBoundsForArea(seal);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!isValidTarget(target)) {
                continue;
            }
            int count = 0;
            for (LivingEntity same : level.getEntitiesOfClass(target.getClass(), area)) {
                if (isValidTarget(same) && ++count >= MIN_HERD_SIZE) {
                    break;
                }
            }
            if (count > MIN_HERD_SIZE - 1) {
                Task task = new Task(seal.getSealPos(), target);
                task.setPriority(seal.getPriority());
                task.setLifespan(TASK_LIFESPAN);
                TaskHandler.addTask(level, task);
                wait = true;
                break;
            }
        }
    }

    private static boolean isValidTarget(LivingEntity target) {
        if (!(target instanceof Animal) && !(target instanceof WaterAnimal)) {
            return false;
        }
        if (target instanceof Enemy || target instanceof AbstractGolem) {
            return false;
        }
        if (target instanceof TamableAnimal tamable && tamable.isTame()) {
            return false;
        }
        return !target.isBaby();
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {
        if (task.getEntity() instanceof LivingEntity target && isValidTarget(target) && golem.getGolemEntity() instanceof Mob mob) {
            mob.setTarget(target);
            golem.addRankXp(1);
        }
        task.setSuspended(true);
        wait = false;
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        task.setSuspended(true);
        wait = false;
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        return true;
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return !level.getBlockState(pos).isAir();
    }

    @Override
    public Identifier getSealIcon() {
        return TCIds.rl("textures/item/seal_butcher.png");
    }

    @Override
    public int[] getGuiCategories() {
        return new int[]{CAT_AREA, CAT_PRIORITY, CAT_TAGS};
    }

    @Override
    public GolemTrait[] getRequiredTags() {
        return new GolemTrait[]{TCGolemTraits.FIGHTER.get(), TCGolemTraits.SMART.get()};
    }

    @Override
    public GolemTrait[] getForbiddenTags() {
        return null;
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {
        wait = false;
    }

    @Override
    public void readCustomNBT(CompoundTag nbt) {}

    @Override
    public void writeCustomNBT(CompoundTag nbt) {}

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {
        wait = false;
    }
}
