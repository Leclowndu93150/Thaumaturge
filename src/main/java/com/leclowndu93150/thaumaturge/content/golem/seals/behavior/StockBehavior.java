package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public final class StockBehavior implements ISealBehavior {
    private static final int STAGGER = 50;
    private static final int SCAN_PERIOD = 20;

    private final SealClock clock = new SealClock(STAGGER);

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.advance() % SCAN_PERIOD != 0) {
            return;
        }
        SealPos at = seal.pos();
        IItemHandler shelf = InvHelper.getItemHandlerAt(level, at.pos(), at.face());
        if (shelf == null) {
            return;
        }
        ISealFilter filter = seal.filter().orElseThrow();
        InvHelper.InvFilter matching = ItemMatchSettings.of(seal);
        for (int slot = 0; slot < filter.spec().slots(); slot++) {
            ItemStack kind = filter.stack(slot);
            if (kind.isEmpty()) {
                continue;
            }
            int missing = filter.limit(slot) - InvHelper.countTotalItemsIn(shelf, kind, matching);
            if (missing > 0) {
                ItemStack order = InvHelper.hasRoomFor(
                        level, at.pos(), at.face(), kind.copyWithCount(Math.min(kind.getMaxStackSize(), missing)));
                if (!order.isEmpty()) {
                    GolemHelper.requestProvisioning(level, at.pos(), at.face(), order);
                }
            }
        }
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        return true;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        return false;
    }
}
