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
import com.leclowndu93150.thaumaturge.content.equipment.EnchantMining;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandler;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import com.leclowndu93150.thaumaturge.server.TCFakePlayer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;

public class SealLumber implements ISeal, ISealGui, ISealConfigArea {
    private static final int CACHE_CLEAN_INTERVAL = 100;

    private int delay = System.identityHashCode(this) % 33;
    private final Map<Integer, Long> cache = new HashMap<>();

    @Override
    public Identifier getKey() {
        return TCIds.rl("lumber");
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay % CACHE_CLEAN_INTERVAL == 0) {
            Iterator<Integer> iterator = cache.keySet().iterator();
            while (iterator.hasNext()) {
                if (TaskHandler.getTask(level, iterator.next()) == null) {
                    iterator.remove();
                }
            }
        }
        delay++;
        BlockPos pos = GolemHelper.getPosInArea(seal, delay);
        if (!cache.containsValue(pos.asLong()) && level.getBlockState(pos).is(BlockTags.LOGS)) {
            Task task = new Task(seal.getSealPos(), pos);
            task.setPriority(seal.getPriority());
            TaskHandler.addTask(level, task);
            cache.put(task.getId(), pos.asLong());
        }
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        if (cache.containsKey(task.getId()) && level.getBlockState(task.getPos()).is(BlockTags.LOGS) && level instanceof ServerLevel serverLevel) {
            golem.swingArm();
            if (EnchantMining.breakFurthest(serverLevel, task.getPos(), level.getBlockState(task.getPos()), TCFakePlayer.GOLEM.at(serverLevel, golem.getGolemEntity()))) {
                task.setLifespan((short) Math.max(task.getLifespan(), 10L));
                golem.addRankXp(1);
                return false;
            }
            cache.remove(task.getId());
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        if (cache.containsKey(task.getId()) && golem.getGolemWorld().getBlockState(task.getPos()).is(BlockTags.LOGS)) {
            return true;
        }
        task.setSuspended(true);
        return false;
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {
        cache.remove(task.getId());
    }

    @Override
    public void readCustomNBT(CompoundTag nbt) {}

    @Override
    public void writeCustomNBT(CompoundTag nbt) {}

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return !level.getBlockState(pos).isAir();
    }

    @Override
    public Identifier getSealIcon() {
        return TCIds.rl("textures/item/seal_lumber.png");
    }

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {}

    @Override
    public int[] getGuiCategories() {
        return new int[]{CAT_AREA, CAT_PRIORITY, CAT_TAGS};
    }

    @Override
    public GolemTrait[] getRequiredTags() {
        return new GolemTrait[]{TCGolemTraits.BREAKER.get(), TCGolemTraits.SMART.get()};
    }

    @Override
    public GolemTrait[] getForbiddenTags() {
        return null;
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {}
}
