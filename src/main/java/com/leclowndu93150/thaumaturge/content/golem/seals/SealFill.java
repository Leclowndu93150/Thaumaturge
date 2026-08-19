package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandler;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class SealFill extends SealFiltered {
    private static final int SCAN_INTERVAL = 20;
    private static final double WORLD_COUNT_RANGE = 1.5;

    protected final ISealConfigToggles.SealToggle[] props = {new ISealConfigToggles.SealToggle(true, "pmeta", "golem.prop.meta"), new ISealConfigToggles.SealToggle(true, "pnbt", "golem.prop.nbt"),
            new ISealConfigToggles.SealToggle(false, "pore", "golem.prop.ore"), new ISealConfigToggles.SealToggle(false, "pmod", "golem.prop.mod"),
            new ISealConfigToggles.SealToggle(false, "pexist", "golem.prop.exist")};

    private int delay = System.identityHashCode(this) % 50;
    private int watchedTask = Integer.MIN_VALUE;

    @Override
    public Identifier getKey() {
        return TCIds.rl("fill");
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay++ % SCAN_INTERVAL != 0) {
            return;
        }
        Task oldTask = TaskHandler.getTask(level, watchedTask);
        if (oldTask == null || oldTask.isReserved() || oldTask.isSuspended() || oldTask.isCompleted()) {
            Task task = new Task(seal.getSealPos(), seal.getSealPos().pos());
            task.setPriority(seal.getPriority());
            TaskHandler.addTask(level, task);
            watchedTask = task.getId();
        }
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {
        ISealEntity seal = SealHandler.getSealEntity(level, task.getSealPos());
        if (seal != null && !seal.isStoppedByRedstone(level)) {
            Task newTask = new Task(task.getSealPos(), task.getSealPos().pos());
            newTask.setPriority(seal.getPriority());
            TaskHandler.addTask(level, newTask);
            watchedTask = newTask.getId();
        }
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        InvHelper.InvFilter flags = filterFlags(props);
        InvHelper.FilterMatch match = InvHelper.findFirstMatchFromFilterWithSize(getInv(), getSizes(), isBlacklist(), golem.getCarrying(), flags);
        if (!match.stack().isEmpty()) {
            ResourceHandler<ItemResource> inv = InvHelper.getItemHandlerAt(level, task.getSealPos().pos(), task.getSealPos().face());
            int limit = match.stack().getCount();
            if (hasStacksizeLimiters() && match.sizeLimit() > 0) {
                int present = inv == null
                        ? InvHelper.countStackInWorld(level, task.getSealPos().pos(), match.stack(), WORLD_COUNT_RANGE, flags)
                        : InvHelper.countTotalItemsIn(inv, match.stack(), flags);
                limit = present < match.sizeLimit() ? match.sizeLimit() - present : 0;
            }
            if (limit > 0) {
                ItemStack toDrop = match.stack().copy();
                toDrop.setCount(limit);
                ItemStack dropped = golem.dropItem(toDrop);
                if (inv == null) {
                    ItemEntity item = new ItemEntity(level, task.getSealPos().pos().getX() + 0.5 + task.getSealPos().face().getStepX(),
                            task.getSealPos().pos().getY() + 0.5 + task.getSealPos().face().getStepY(), task.getSealPos().pos().getZ() + 0.5 + task.getSealPos().face().getStepZ(), dropped);
                    item.setDeltaMovement(item.getDeltaMovement().multiply(0.2, 0.5, 0.2));
                    level.addFreshEntity(item);
                } else {
                    golem.holdItem(InvHelper.insertStack(inv, dropped, false));
                }
                golem.getGolemEntity().playSound(SoundEvents.ITEM_PICKUP, 0.125F, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F);
                golem.addRankXp(1);
                golem.swingArm();
            }
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        InvHelper.InvFilter flags = filterFlags(props);
        InvHelper.FilterMatch match = InvHelper.findFirstMatchFromFilterWithSize(getInv(), getSizes(), isBlacklist(), golem.getCarrying(), flags);
        if (match.stack().isEmpty()) {
            return false;
        }
        ResourceHandler<ItemResource> inv = InvHelper.getItemHandlerAt(golem.getGolemWorld(), task.getSealPos().pos(), task.getSealPos().face());
        if (inv != null) {
            if (props[4].getValue() && InvHelper.countTotalItemsIn(inv, match.stack(), flags) <= 0) {
                return false;
            }
            if (InvHelper.hasRoomForSome(golem.getGolemWorld(), task.getSealPos().pos(), task.getSealPos().face(), match.stack())) {
                if (!hasStacksizeLimiters() || match.sizeLimit() <= 0) {
                    return true;
                }
                return InvHelper.countTotalItemsIn(inv, match.stack(), flags) < match.sizeLimit();
            }
            return false;
        }
        if (hasStacksizeLimiters() && match.sizeLimit() > 0) {
            return InvHelper.countStackInWorld(golem.getGolemWorld(), task.getSealPos().pos(), match.stack(), WORLD_COUNT_RANGE, flags) < match.sizeLimit();
        }
        return true;
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return !level.getBlockState(pos).isAir();
    }

    @Override
    public Identifier getSealIcon() {
        return TCIds.rl("textures/item/seal_fill.png");
    }

    @Override
    public int[] getGuiCategories() {
        return new int[]{CAT_FILTER, CAT_PRIORITY, CAT_TAGS};
    }

    @Override
    public GolemTrait[] getRequiredTags() {
        return null;
    }

    @Override
    public GolemTrait[] getForbiddenTags() {
        return new GolemTrait[]{TCGolemTraits.CLUMSY.get()};
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {}

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {}

    @Override
    public boolean hasStacksizeLimiters() {
        return !isBlacklist();
    }
}
