package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.golem.GolemInteractionHelper;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class UseBehavior implements ISealBehavior {
    public static final SealSetting LEFT_CLICK = new SealSetting("pleft", "golem.prop.left", false);
    public static final SealSetting INTO_AIR = new SealSetting("pempty", "golem.prop.empty", false);
    public static final SealSetting BARE_HANDED = new SealSetting("pemptyhand", "golem.prop.emptyhand", false);
    public static final SealSetting SNEAKING = new SealSetting("psneak", "golem.prop.sneak", false);
    public static final SealSetting REQUEST_ITEMS = new SealSetting("ppro", "golem.prop.provision.wl", false);

    private static final int STAGGER = 49;
    private static final int SCAN_PERIOD = 5;

    private final SealClock clock = new SealClock(STAGGER);
    private int pendingTask = Integer.MIN_VALUE;

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.advance() % SCAN_PERIOD != 0) {
            return;
        }
        Task pending = TaskBoard.of(level).find(pendingTask);
        if (pending != null && !pending.isSuspended() && !pending.isCompleted() || !targetReady(level, seal)) {
            return;
        }
        Task task = Task.atBlock(seal.pos(), seal.pos().pos());
        task.setPriority(seal.priority());
        TaskBoard.of(level).post(task);
        pendingTask = task.id();
    }

    private static boolean targetReady(Level level, ISealEntity seal) {
        return seal.setting(INTO_AIR) == level.getBlockState(seal.pos().pos()).isAir();
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        if (targetReady(level, seal)) {
            ItemStack tool = pickTool(seal, golem);
            boolean bareHanded = seal.setting(BARE_HANDED);
            if (!tool.isEmpty() || bareHanded) {
                ItemStack used = tool.copy();
                if (!tool.isEmpty()) {
                    golem.hands().release(tool.copy());
                }
                GolemInteractionHelper.golemClick(level, golem, task.pos(), seal.pos().face(), bareHanded ? ItemStack.EMPTY : used, seal.setting(SNEAKING), !seal.setting(LEFT_CLICK));
            }
        }
        task.suspend();
        return true;
    }

    private static ItemStack pickTool(ISealEntity seal, IGolemAPI golem) {
        ISealFilter filter = seal.filter().orElseThrow();
        return filter.stack(0).isEmpty() ? golem.hands().contents().get(0) : matchCarried(seal, golem);
    }

    private static ItemStack matchCarried(ISealEntity seal, IGolemAPI golem) {
        ISealFilter filter = seal.filter().orElseThrow();
        return InvHelper.findFirstMatchFromFilter(filter.stacks(), filter.limits(), filter.isBlacklist(), golem.hands().contents(), ItemMatchSettings.of(seal));
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        if (seal.setting(BARE_HANDED)) {
            return true;
        }
        boolean carrying = !matchCarried(seal, golem).isEmpty();
        ISealFilter filter = seal.filter().orElseThrow();
        if (!carrying && seal.setting(REQUEST_ITEMS) && !filter.isBlacklist() && !filter.stack(0).isEmpty()) {
            GolemHelper.requestProvisioning(golem.level(), seal, filter.stack(0).copy());
        }
        return carrying;
    }
}
