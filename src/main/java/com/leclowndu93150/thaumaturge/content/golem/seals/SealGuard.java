package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISeal;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigArea;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealGui;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandler;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class SealGuard implements ISeal, ISealGui, ISealConfigArea {
    private static final int SCAN_INTERVAL = 20;
    private static final short TASK_LIFESPAN = 10;

    protected final ISealConfigToggles.SealToggle[] props = {new ISealConfigToggles.SealToggle(true, "pmob", "golem.prop.mob"),
            new ISealConfigToggles.SealToggle(false, "panimal", "golem.prop.animal"), new ISealConfigToggles.SealToggle(false, "pplayer", "golem.prop.player")};

    private int delay = System.identityHashCode(this) % 22;

    @Override
    public Identifier getKey() {
        return TCIds.rl("guard");
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay++ % SCAN_INTERVAL != 0) {
            return;
        }
        AABB area = GolemHelper.getBoundsForArea(seal);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (isValidTarget(level, target)) {
                Task task = new Task(seal.getSealPos(), target);
                task.setPriority(seal.getPriority());
                task.setLifespan(TASK_LIFESPAN);
                TaskHandler.addTask(level, task);
            }
        }
    }

    private boolean isValidTarget(Level level, LivingEntity target) {
        if (props[0].getValue() && target instanceof Enemy) {
            return true;
        }
        if (props[1].getValue() && (target instanceof Animal || target instanceof WaterAnimal)) {
            return true;
        }
        return props[2].getValue() && level instanceof ServerLevel serverLevel && serverLevel.isPvpAllowed() && target instanceof Player;
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {
        if (task.getEntity() instanceof LivingEntity target && isValidTarget(level, target) && golem.getGolemEntity() instanceof Mob mob) {
            mob.setTarget(target);
            golem.addRankXp(1);
        }
        task.setSuspended(true);
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        return task.getEntity() != null && !golem.getGolemEntity().isAlliedTo(task.getEntity());
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return !level.getBlockState(pos).isAir();
    }

    @Override
    public Identifier getSealIcon() {
        return TCIds.rl("textures/item/seal_guard.png");
    }

    @Override
    public int[] getGuiCategories() {
        return new int[]{CAT_AREA, CAT_PRIORITY, CAT_TAGS};
    }

    @Override
    public GolemTrait[] getRequiredTags() {
        return new GolemTrait[]{TCGolemTraits.FIGHTER.get()};
    }

    @Override
    public GolemTrait[] getForbiddenTags() {
        return null;
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {}

    @Override
    public void readCustomNBT(CompoundTag nbt) {}

    @Override
    public void writeCustomNBT(CompoundTag nbt) {}

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {}
}
