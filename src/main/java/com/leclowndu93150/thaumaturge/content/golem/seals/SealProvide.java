package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.ProvisionRequest;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandler;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

public class SealProvide extends SealFiltered implements ISealConfigToggles {
    private static final int SCAN_INTERVAL = 20;
    private static final int CLEAN_INTERVAL = 100;
    private static final double PROVISION_RANGE_SQR = 4096.0;
    private static final short SEAL_TASK_LIFESPAN = 10;
    private static final short DELIVERY_TASK_LIFESPAN = 31000;
    private static final byte DATA_COLLECT = 0;
    private static final byte DATA_DELIVER_ENTITY = 1;
    private static final byte DATA_DELIVER_POS = 2;

    protected final ISealConfigToggles.SealToggle[] props = {
        new ISealConfigToggles.SealToggle(true, "pmeta", "golem.prop.meta"),
        new ISealConfigToggles.SealToggle(true, "pnbt", "golem.prop.nbt"),
        new ISealConfigToggles.SealToggle(false, "pore", "golem.prop.ore"),
        new ISealConfigToggles.SealToggle(false, "pmod", "golem.prop.mod"),
        new ISealConfigToggles.SealToggle(false, "psing", "golem.prop.single"),
        new ISealConfigToggles.SealToggle(false, "pleave", "golem.prop.leave")
    };

    private int delay = System.identityHashCode(this) % 88;

    @Override
    public ResourceLocation getKey() {
        return TCIds.rl("provider");
    }

    @Override
    public int getFilterSize() {
        return 9;
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        List<ProvisionRequest> requests = GolemHelper.getProvisionRequests(level);
        if (delay % CLEAN_INTERVAL == 0) {
            requests.removeIf(request -> request.isInvalid()
                    || request.getLinkedTask() == null
                    || request.getLinkedTask().isSuspended()
                    || request.getLinkedTask().isCompleted()
                    || request.getTimeout() < level.getGameTime());
        }
        if (delay++ % SCAN_INTERVAL != 0) {
            return;
        }
        IItemHandler inv = InvHelper.getItemHandlerAt(
                level, seal.getSealPos().pos(), seal.getSealPos().face());
        if (inv == null) {
            return;
        }
        requests.removeIf(ProvisionRequest::isInvalid);
        for (ProvisionRequest request : requests) {
            if (request.getLinkedTask() != null || !isInRange(seal, request)) {
                continue;
            }
            boolean filterMatch = !InvHelper.findFirstMatchFromFilter(
                            getInv(), getSizes(), blacklist, List.of(request.getStack()), filterFlags(props))
                    .isEmpty();
            if (filterMatch
                    && InvHelper.countTotalItemsIn(inv, request.getStack(), InvHelper.InvFilter.STRICT)
                            > (props[5].getValue() ? 1 : 0)) {
                Task task = new Task(seal.getSealPos(), seal.getSealPos().pos());
                task.setPriority(request.getSeal() != null ? request.getSeal().getPriority() : 5);
                task.setLifespan(request.getSeal() != null ? SEAL_TASK_LIFESPAN : DELIVERY_TASK_LIFESPAN);
                TaskHandler.addTask(level, task);
                request.setLinkedTask(task);
                task.setLinkedProvision(request);
                break;
            }
        }
    }

    private static boolean isInRange(ISealEntity seal, ProvisionRequest request) {
        BlockPos sealPos = seal.getSealPos().pos();
        if (request.getSeal() != null) {
            return request.getSeal().getSealPos().pos().distSqr(sealPos) < PROVISION_RANGE_SQR;
        }
        if (request.getEntity() != null) {
            return sealPos.distToCenterSqr(
                            request.getEntity().getX(),
                            request.getEntity().getY(),
                            request.getEntity().getZ())
                    < PROVISION_RANGE_SQR;
        }
        return request.getPos() != null && request.getPos().distSqr(sealPos) < PROVISION_RANGE_SQR;
    }

    public boolean matchesFilters(ItemStack stack) {
        return InvHelper.matchesFilters(getInv(), blacklist, stack, filterFlags(props));
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        ProvisionRequest request = task.getLinkedProvision();
        if (request != null) {
            if (task.getData() == DATA_COLLECT) {
                collectForDelivery(level, golem, task, request);
            } else {
                deliver(level, golem, task, request);
            }
        }
        task.setSuspended(true);
        return true;
    }

    private void collectForDelivery(Level level, IGolemAPI golem, Task task, ProvisionRequest request) {
        IItemHandler inv = InvHelper.getItemHandlerAt(
                level, task.getSealPos().pos(), task.getSealPos().face());
        if (inv == null) {
            return;
        }
        ItemStack stack = request.getStack().copy();
        if (props[4].getValue()) {
            stack.setCount(1);
        }
        if (!stack.isEmpty() && props[5].getValue()) {
            int available = InvHelper.countTotalItemsIn(inv, stack, InvHelper.InvFilter.STRICT);
            if (available <= stack.getCount()) {
                stack.setCount(available - 1);
            }
        }
        if (stack.isEmpty()) {
            return;
        }
        int limit = golem.canCarryAmount(stack);
        if (limit <= 0) {
            return;
        }
        ItemStack remainder = golem.holdItem(InvHelper.removeStackFrom(
                inv, InvHelper.copyLimitedStack(stack, limit), InvHelper.InvFilter.STRICT, false));
        if (!remainder.isEmpty()) {
            InvHelper.ejectStackAt(
                    level,
                    task.getSealPos().pos().relative(task.getSealPos().face()),
                    task.getSealPos().face().getOpposite(),
                    remainder);
        }
        golem.getGolemEntity()
                .playSound(
                        SoundEvents.ITEM_PICKUP,
                        0.125F,
                        ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        golem.addRankXp(1);
        golem.swingArm();
        if (request.getEntity() != null || request.getPos() != null) {
            Task deliveryTask = request.getEntity() != null
                    ? new Task(task.getSealPos(), request.getEntity())
                    : new Task(task.getSealPos(), request.getPos());
            deliveryTask.setPriority(task.getPriority());
            deliveryTask.setData(request.getEntity() != null ? DATA_DELIVER_ENTITY : DATA_DELIVER_POS);
            deliveryTask.setLifespan(DELIVERY_TASK_LIFESPAN);
            TaskHandler.addTask(level, deliveryTask);
            request.setLinkedTask(deliveryTask);
            deliveryTask.setLinkedProvision(request);
        }
    }

    private void deliver(Level level, IGolemAPI golem, Task task, ProvisionRequest request) {
        ItemStack requested = request.getStack();
        ItemStack carried = golem.dropItem(requested);
        if (carried.getCount() < requested.getCount()) {
            ItemStack missing = requested.copy();
            missing.setCount(requested.getCount() - carried.getCount());
            if (task.getData() == DATA_DELIVER_ENTITY) {
                GolemHelper.requestProvisioning(level, request.getEntity(), missing);
            } else {
                GolemHelper.requestProvisioning(level, request.getPos(), request.getSide(), missing);
            }
        }
        if (task.getData() == DATA_DELIVER_ENTITY) {
            InvHelper.dropItemAtEntity(level, carried, request.getEntity());
        } else {
            ItemStack rejected = InvHelper.ejectStackAt(
                    level,
                    request.getPos().relative(request.getSide()),
                    request.getSide().getOpposite(),
                    carried,
                    true);
            if (!rejected.isEmpty()) {
                golem.holdItem(rejected);
            }
        }
        golem.getGolemEntity()
                .playSound(
                        SoundEvents.ITEM_PICKUP,
                        0.125F,
                        (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F);
        golem.swingArm();
        request.setInvalid(true);
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        ProvisionRequest request = task.getLinkedProvision();
        if (request == null || !(golem.getGolemEntity() instanceof EntityThaumaturgeGolem golemEntity)) {
            return false;
        }
        boolean inHomeRange = request.getSeal() != null
                        && golemEntity.isWithinRestriction(
                                request.getSeal().getSealPos().pos())
                || request.getEntity() != null
                        && golemEntity.isWithinRestriction(request.getEntity().blockPosition())
                || request.getPos() != null && golemEntity.isWithinRestriction(request.getPos());
        if (!inHomeRange || !areGolemTagsValidForTask(request.getSeal(), golem)) {
            return false;
        }
        if (task.getData() == DATA_COLLECT) {
            return !golem.isCarrying(request.getStack()) && golem.canCarry(request.getStack(), true);
        }
        return golem.isCarrying(request.getStack());
    }

    private boolean areGolemTagsValidForTask(ISealEntity seal, IGolemAPI golem) {
        if (seal == null) {
            return true;
        }
        if (golem.getGolemEntity() instanceof EntityThaumaturgeGolem golemEntity && seal.isLocked()) {
            if (golemEntity.getOwnerUUID() == null
                    || !golemEntity.getOwnerUUID().equals(seal.getOwner())) {
                return false;
            }
        }
        GolemTrait[] required = seal.getSeal().getRequiredTags();
        if (required != null && !golem.getProperties().getTraits().containsAll(List.of(required))) {
            return false;
        }
        GolemTrait[] forbidden = seal.getSeal().getForbiddenTags();
        if (forbidden != null) {
            for (GolemTrait tag : forbidden) {
                if (golem.getProperties().getTraits().contains(tag)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {
        if (task.getLinkedProvision() != null) {
            task.getLinkedProvision().setLinkedTask(null);
        }
        task.setLinkedProvision(null);
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return InvHelper.getItemHandlerAt(level, pos, side) != null;
    }

    @Override
    public ResourceLocation getSealIcon() {
        return TCIds.rl("textures/item/seal_provider.png");
    }

    @Override
    public int[] getGuiCategories() {
        return new int[] {CAT_FILTER, CAT_TOGGLES, CAT_PRIORITY, CAT_TAGS};
    }

    @Override
    public GolemTrait[] getRequiredTags() {
        return null;
    }

    @Override
    public GolemTrait[] getForbiddenTags() {
        return new GolemTrait[] {TCGolemTraits.CLUMSY.get()};
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {}

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {}

    @Override
    public ISealConfigToggles.SealToggle[] getToggles() {
        return props;
    }

    @Override
    public void setToggle(int index, boolean value) {
        props[index].setValue(value);
    }
}
