package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.golem.GolemInteractionHelper;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandler;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SealUse extends SealFiltered implements ISealConfigToggles {
    private static final int SCAN_INTERVAL = 5;

    protected final ISealConfigToggles.SealToggle[] props = {new ISealConfigToggles.SealToggle(true, "pmeta", "golem.prop.meta"), new ISealConfigToggles.SealToggle(true, "pnbt", "golem.prop.nbt"),
            new ISealConfigToggles.SealToggle(false, "pore", "golem.prop.ore"), new ISealConfigToggles.SealToggle(false, "pmod", "golem.prop.mod"),
            new ISealConfigToggles.SealToggle(false, "pleft", "golem.prop.left"), new ISealConfigToggles.SealToggle(false, "pempty", "golem.prop.empty"),
            new ISealConfigToggles.SealToggle(false, "pemptyhand", "golem.prop.emptyhand"), new ISealConfigToggles.SealToggle(false, "psneak", "golem.prop.sneak"),
            new ISealConfigToggles.SealToggle(false, "ppro", "golem.prop.provision.wl")};

    private int delay = System.identityHashCode(this) % 49;
    private int watchedTask = Integer.MIN_VALUE;

    @Override
    public Identifier getKey() {
        return TCIds.rl("use");
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay++ % SCAN_INTERVAL != 0) {
            return;
        }
        Task oldTask = TaskHandler.getTask(level, watchedTask);
        if (oldTask != null && !oldTask.isSuspended() && !oldTask.isCompleted()) {
            return;
        }
        if (getToggles()[5].getValue() != level.getBlockState(seal.getSealPos().pos()).isAir()) {
            return;
        }
        Task task = new Task(seal.getSealPos(), seal.getSealPos().pos());
        task.setPriority(seal.getPriority());
        TaskHandler.addTask(level, task);
        watchedTask = task.getId();
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {}

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        if (getToggles()[5].getValue() == level.getBlockState(task.getPos()).isAir()) {
            ItemStack clickStack = golem.getCarrying().get(0);
            if (!filter.get(0).isEmpty()) {
                clickStack = InvHelper.findFirstMatchFromFilter(filter, filterSize, blacklist, golem.getCarrying(), filterFlags(props));
            }
            if (!clickStack.isEmpty() || props[6].getValue()) {
                ItemStack held = ItemStack.EMPTY;
                if (!clickStack.isEmpty()) {
                    held = clickStack.copy();
                    golem.dropItem(clickStack.copy());
                }
                GolemInteractionHelper.golemClick(level, golem, task.getPos(), task.getSealPos().face(), props[6].getValue() ? ItemStack.EMPTY : held, props[7].getValue(),
                        !getToggles()[4].getValue());
            }
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        if (props[6].getValue()) {
            return true;
        }
        boolean found = !InvHelper.findFirstMatchFromFilter(filter, filterSize, blacklist, golem.getCarrying(), filterFlags(props)).isEmpty();
        if (!found && getToggles()[8].getValue() && !blacklist && !getInv().get(0).isEmpty()) {
            ISealEntity seal = SealHandler.getSealEntity(golem.getGolemWorld(), task.getSealPos());
            if (seal != null) {
                GolemHelper.requestProvisioning(golem.getGolemWorld(), seal, getInv().get(0).copy());
            }
        }
        return found;
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {}

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public Identifier getSealIcon() {
        return TCIds.rl("textures/item/seal_use.png");
    }

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {}

    @Override
    public int[] getGuiCategories() {
        return new int[]{CAT_FILTER, CAT_TOGGLES, CAT_PRIORITY, CAT_TAGS};
    }

    @Override
    public GolemTrait[] getRequiredTags() {
        return new GolemTrait[]{TCGolemTraits.DEFT.get(), TCGolemTraits.SMART.get()};
    }

    @Override
    public GolemTrait[] getForbiddenTags() {
        return null;
    }

    @Override
    public ISealConfigToggles.SealToggle[] getToggles() {
        return props;
    }

    @Override
    public void setToggle(int index, boolean value) {
        props[index].setValue(value);
    }
}
