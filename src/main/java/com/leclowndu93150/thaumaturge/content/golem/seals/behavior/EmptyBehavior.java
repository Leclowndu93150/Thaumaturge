package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public final class EmptyBehavior implements ISealBehavior {
    public static final SealSetting CYCLE = new SealSetting("pcycle", "gui.thaumaturge.seal.setting.cycle", false);
    public static final SealSetting LEAVE_ONE = new SealSetting("pleave", "gui.thaumaturge.seal.setting.leave", false);

    private static final int STAGGER = 30;
    private static final int SCAN_PERIOD = 20;
    private static final int TIDY_PERIOD = 100;
    private static final int TASK_LIFESPAN = 5;

    private final SealClock clock = new SealClock(STAGGER);
    private final TaskLedger<ItemStack> takings = new TaskLedger<>();
    private int turn;

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.at(TIDY_PERIOD)) {
            takings.dropFinished(level);
        }
        if (clock.advance() % SCAN_PERIOD != 0) {
            return;
        }
        IItemHandler source =
                InvHelper.getItemHandlerAt(level, seal.pos().pos(), seal.pos().face());
        if (source == null) {
            return;
        }
        ISealFilter filter = seal.filter().orElseThrow();
        ItemStack found = InvHelper.findFirstMatchFromFilter(
                candidates(seal, filter),
                filter.isBlacklist(),
                source,
                ItemMatchSettings.of(seal),
                seal.setting(LEAVE_ONE));
        if (!found.isEmpty()) {
            Task task = Task.atBlock(seal.pos(), seal.pos().pos());
            task.setPriority(seal.priority());
            task.setLife(TASK_LIFESPAN);
            TaskBoard.of(level).post(task);
            takings.record(task, found);
        }
    }

    private List<ItemStack> candidates(ISealEntity seal, ISealFilter filter) {
        if (!seal.setting(CYCLE) || filter.isBlacklist()) {
            return filter.stacks();
        }
        List<ItemStack> listed =
                filter.stacks().stream().filter(stack -> !stack.isEmpty()).toList();
        return listed.isEmpty() ? filter.stacks() : List.of(listed.get(Math.abs(turn % listed.size())));
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        ItemStack wanted = takings.get(task);
        SealPos at = seal.pos();
        InvHelper.InvFilter matching = ItemMatchSettings.of(seal);
        if (wanted != null && !wanted.isEmpty() && seal.setting(LEAVE_ONE)) {
            int stored = InvHelper.countTotalItemsIn(level, at.pos(), at.face(), wanted, matching);
            if (stored <= wanted.getCount()) {
                wanted = wanted.copyWithCount(stored - 1);
            }
        }
        if (wanted != null && !wanted.isEmpty()) {
            int room = golem.hands().room(wanted);
            if (room > 0) {
                ItemStack taken = InvHelper.removeStackFrom(
                        level, at.pos(), at.face(), InvHelper.copyLimitedStack(wanted, room), matching, false);
                ItemStack spill = golem.hands().hold(taken);
                if (!spill.isEmpty()) {
                    InvHelper.ejectStackAt(
                            level, at.pos().relative(at.face()), at.face().getOpposite(), spill);
                }
                HandlingSound.play(golem, HandlingSound.HIGH);
                golem.swingArm();
            }
        }
        takings.forget(task);
        turn++;
        task.end();
        return true;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ItemStack wanted = takings.get(task);
        return wanted != null && !wanted.isEmpty() && golem.hands().canTake(wanted, true);
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        takings.forget(task);
    }
}
