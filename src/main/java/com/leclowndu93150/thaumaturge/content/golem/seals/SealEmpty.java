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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class SealEmpty extends SealFiltered {
    private static final int SCAN_INTERVAL = 20;
    private static final int CACHE_CLEAN_INTERVAL = 100;
    private static final short TASK_LIFESPAN = 5;

    protected final ISealConfigToggles.SealToggle[] props = {new ISealConfigToggles.SealToggle(true, "pmeta", "golem.prop.meta"), new ISealConfigToggles.SealToggle(true, "pnbt", "golem.prop.nbt"),
            new ISealConfigToggles.SealToggle(false, "pore", "golem.prop.ore"), new ISealConfigToggles.SealToggle(false, "pmod", "golem.prop.mod"),
            new ISealConfigToggles.SealToggle(false, "pcycle", "golem.prop.cycle"), new ISealConfigToggles.SealToggle(false, "pleave", "golem.prop.leave")};

    private int delay = System.identityHashCode(this) % 30;
    private int filterInc;
    private final Map<Integer, ItemStack> cache = new HashMap<>();

    @Override
    public Identifier getKey() {
        return TCIds.rl("empty");
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
        if (delay++ % SCAN_INTERVAL != 0) {
            return;
        }
        ResourceHandler<ItemResource> handler = InvHelper.getItemHandlerAt(level, seal.getSealPos().pos(), seal.getSealPos().face());
        if (handler == null) {
            return;
        }
        ItemStack stack = InvHelper.findFirstMatchFromFilter(getInv(filterInc), isBlacklist(), handler, filterFlags(props), props[5].getValue());
        if (!stack.isEmpty()) {
            Task task = new Task(seal.getSealPos(), seal.getSealPos().pos());
            task.setPriority(seal.getPriority());
            task.setLifespan(TASK_LIFESPAN);
            TaskHandler.addTask(level, task);
            cache.put(task.getId(), stack);
        }
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        ItemStack stack = cache.get(task.getId());
        if (stack != null && !stack.isEmpty()) {
            int available = InvHelper.countTotalItemsIn(level, task.getSealPos().pos(), task.getSealPos().face(), stack, filterFlags(props));
            if (props[5].getValue() && available <= stack.getCount()) {
                stack = stack.copy();
                stack.setCount(available - 1);
            }
        }
        if (stack != null && !stack.isEmpty()) {
            int limit = golem.canCarryAmount(stack);
            if (limit > 0) {
                ItemStack removed = InvHelper.removeStackFrom(level, task.getSealPos().pos(), task.getSealPos().face(), InvHelper.copyLimitedStack(stack, limit), filterFlags(props), false);
                ItemStack remainder = golem.holdItem(removed);
                if (!remainder.isEmpty()) {
                    InvHelper.ejectStackAt(level, task.getSealPos().pos().relative(task.getSealPos().face()), task.getSealPos().face().getOpposite(), remainder);
                }
                golem.getGolemEntity().playSound(SoundEvents.ITEM_PICKUP, 0.125F, ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                golem.swingArm();
            }
        }
        cache.remove(task.getId());
        filterInc++;
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        ItemStack stack = cache.get(task.getId());
        return stack != null && !stack.isEmpty() && golem.canCarry(stack, true);
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return InvHelper.getItemHandlerAt(level, pos, side) != null;
    }

    protected List<ItemStack> getInv(int cycle) {
        return getInv();
    }

    @Override
    public Identifier getSealIcon() {
        return TCIds.rl("textures/item/seal_empty.png");
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
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {}

    @Override
    public void onTaskSuspension(Level level, Task task) {
        cache.remove(task.getId());
    }

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {}
}
