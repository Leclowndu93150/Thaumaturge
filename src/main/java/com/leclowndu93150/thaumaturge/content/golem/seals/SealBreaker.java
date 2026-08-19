package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigArea;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandler;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import com.leclowndu93150.thaumaturge.server.TCFakePlayer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class SealBreaker extends SealFiltered implements ISealConfigArea, ISealConfigToggles {
    private static final int CACHE_CLEAN_INTERVAL = 100;
    private static final int BREAK_SPEED = 21;
    private static final int BREAK_SPEED_SILK = 7;

    protected ISealConfigToggles.SealToggle[] props = {new ISealConfigToggles.SealToggle(true, "pmeta", "golem.prop.meta")};

    private int delay = System.identityHashCode(this) % 42;
    private final Map<Integer, Long> cache = new HashMap<>();

    @Override
    public Identifier getKey() {
        return TCIds.rl("breaker");
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
        if (!cache.containsValue(pos.asLong()) && isValidBlock(level, pos)) {
            Task task = new Task(seal.getSealPos(), pos);
            task.setPriority(seal.getPriority());
            task.setData((int) (level.getBlockState(pos).getDestroySpeed(level, pos) * 10.0F));
            TaskHandler.addTask(level, task);
            cache.put(task.getId(), pos.asLong());
        }
    }

    private boolean isValidBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        ItemStack representative = new ItemStack(state.getBlock());
        for (ItemStack ghost : getInv()) {
            if (ghost.isEmpty()) {
                continue;
            }
            boolean matches = !representative.isEmpty() && ItemStack.isSameItem(representative, ghost);
            if (isBlacklist()) {
                if (matches) {
                    return false;
                }
            } else if (!matches) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        BlockState state = level.getBlockState(task.getPos());
        if (cache.containsKey(task.getId()) && isValidBlock(level, task.getPos()) && level instanceof ServerLevel serverLevel) {
            golem.swingArm();
            boolean silky = getToggles().length > 1 && getToggles()[1].getValue();
            int breakSpeed = silky ? BREAK_SPEED_SILK : BREAK_SPEED;
            if (task.getData() > breakSpeed) {
                float hardness = state.getDestroySpeed(level, task.getPos()) * 10.0F;
                task.setLifespan((short) Math.max(task.getLifespan(), 10L));
                task.setData(task.getData() - breakSpeed);
                int progress = (int) (9.0F * (1.0F - task.getData() / hardness));
                SoundType sound = state.getSoundType();
                level.playSound(null, task.getPos(), sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 0.7F) / 8.0F, sound.getPitch() * 0.5F);
                level.destroyBlockProgress(golem.getGolemEntity().getId(), task.getPos(), progress);
                return false;
            }
            level.destroyBlockProgress(golem.getGolemEntity().getId(), task.getPos(), 10);
            BlockBreakerEngine.harvestBlock(serverLevel, TCFakePlayer.GOLEM.at(serverLevel, golem.getGolemEntity()), task.getPos(), silky, 0);
            golem.addRankXp(1);
            cache.remove(task.getId());
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        if (cache.containsKey(task.getId()) && isValidBlock(golem.getGolemWorld(), task.getPos())) {
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
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return !level.getBlockState(pos).isAir();
    }

    @Override
    public Identifier getSealIcon() {
        return TCIds.rl("textures/item/seal_breaker.png");
    }

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {}

    @Override
    public int[] getGuiCategories() {
        return new int[]{CAT_AREA, CAT_FILTER, CAT_TOGGLES, CAT_PRIORITY, CAT_TAGS};
    }

    @Override
    public GolemTrait[] getRequiredTags() {
        return new GolemTrait[]{TCGolemTraits.BREAKER.get()};
    }

    @Override
    public GolemTrait[] getForbiddenTags() {
        return null;
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {}

    @Override
    public ISealConfigToggles.SealToggle[] getToggles() {
        return props;
    }

    @Override
    public void setToggle(int index, boolean value) {
        props[index].setValue(value);
    }
}
