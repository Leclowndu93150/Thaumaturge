package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class SealStock extends SealFiltered implements ISealConfigToggles {
    private static final int SCAN_INTERVAL = 20;

    protected final ISealConfigToggles.SealToggle[] props = {new ISealConfigToggles.SealToggle(true, "pmeta", "golem.prop.meta"), new ISealConfigToggles.SealToggle(true, "pnbt", "golem.prop.nbt"),
            new ISealConfigToggles.SealToggle(false, "pore", "golem.prop.ore"), new ISealConfigToggles.SealToggle(false, "pmod", "golem.prop.mod")};

    private int delay = System.identityHashCode(this) % 50;

    @Override
    public Identifier getKey() {
        return TCIds.rl("stock");
    }

    @Override
    public int getFilterSize() {
        return 9;
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay++ % SCAN_INTERVAL != 0) {
            return;
        }
        ResourceHandler<ItemResource> inv = InvHelper.getItemHandlerAt(level, seal.getSealPos().pos(), seal.getSealPos().face());
        if (inv == null) {
            return;
        }
        for (int slot = 0; slot < getFilterSize(); slot++) {
            if (getFilterSlot(slot).isEmpty()) {
                continue;
            }
            int amount = InvHelper.countTotalItemsIn(inv, getFilterSlot(slot), filterFlags(props));
            if (amount < getFilterSlotSize(slot)) {
                ItemStack wanted = getFilterSlot(slot).copy();
                wanted.setCount(Math.min(wanted.getMaxStackSize(), getFilterSlotSize(slot) - amount));
                wanted = InvHelper.hasRoomFor(level, seal.getSealPos().pos(), seal.getSealPos().face(), wanted);
                if (!wanted.isEmpty()) {
                    GolemHelper.requestProvisioning(level, seal.getSealPos().pos(), seal.getSealPos().face(), wanted);
                }
            }
        }
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {}

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        return false;
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return InvHelper.getItemHandlerAt(level, pos, side) != null;
    }

    @Override
    public Identifier getSealIcon() {
        return TCIds.rl("textures/item/seal_stock.png");
    }

    @Override
    public int[] getGuiCategories() {
        return new int[]{CAT_FILTER, CAT_TOGGLES, CAT_PRIORITY, CAT_TAGS};
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
        return true;
    }

    @Override
    public boolean isBlacklist() {
        return false;
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
