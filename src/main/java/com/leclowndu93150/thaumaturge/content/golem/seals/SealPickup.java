package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigArea;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class SealPickup extends SealFiltered implements ISealConfigArea {
    private static final int SCAN_INTERVAL = 5;
    private static final byte EVENT_EMOTE_TASK = 5;

    protected final ISealConfigToggles.SealToggle[] props = {new ISealConfigToggles.SealToggle(true, "pmeta", "golem.prop.meta"), new ISealConfigToggles.SealToggle(true, "pnbt", "golem.prop.nbt"),
            new ISealConfigToggles.SealToggle(false, "pore", "golem.prop.ore"), new ISealConfigToggles.SealToggle(false, "pmod", "golem.prop.mod")};

    private int delay = System.identityHashCode(this) % 100;
    private final Map<Integer, Integer> itemEntities = new HashMap<>();

    @Override
    public Identifier getKey() {
        return TCIds.rl("pickup");
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay++ % SCAN_INTERVAL != 0) {
            return;
        }
        AABB area = GolemHelper.getBoundsForArea(seal);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);
        for (ItemEntity item : items) {
            if (item.onGround() && !item.hasPickUpDelay() && !item.getItem().isEmpty() && !itemEntities.containsValue(item.getId())) {
                ItemStack match = InvHelper.findFirstMatchFromFilter(filter, filterSize, isBlacklist(), List.of(item.getItem()), filterFlags(props));
                if (!match.isEmpty()) {
                    Task task = new Task(seal.getSealPos(), item);
                    task.setPriority(seal.getPriority());
                    TaskHandler.addTask(level, task);
                    itemEntities.put(task.getId(), item.getId());
                    break;
                }
            }
        }
        Iterator<Integer> iterator = itemEntities.values().iterator();
        while (iterator.hasNext()) {
            Entity entity = level.getEntity(iterator.next());
            if (entity == null || !entity.isAlive()) {
                iterator.remove();
            }
        }
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        ItemEntity item = getItemEntity(level, task);
        if (item != null && !item.getItem().isEmpty()) {
            ItemStack match = InvHelper.findFirstMatchFromFilter(filter, filterSize, isBlacklist(), List.of(item.getItem()), filterFlags(props));
            if (!match.isEmpty()) {
                ItemStack remainder = golem.holdItem(item.getItem());
                if (!remainder.isEmpty()) {
                    item.setItem(remainder);
                } else {
                    item.discard();
                }
                golem.getGolemEntity().playSound(SoundEvents.ITEM_PICKUP, 0.125F, ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                golem.swingArm();
            }
        }
        task.setSuspended(true);
        itemEntities.remove(task.getId());
        for (Task next : TaskHandler.getEntityTasksSorted(level, null, golem.getGolemEntity())) {
            if (itemEntities.containsKey(next.getId()) && next.canGolemPerformTask(golem) && golem.getGolemEntity() instanceof EntityThaumaturgeGolem golemEntity
                    && golemEntity.isWithinHome(next.getEntity().blockPosition())) {
                golemEntity.setTask(next);
                next.setReserved(true);
                if (ThaumaturgeCommonConfig.SHOW_GOLEM_EMOTES.get()) {
                    level.broadcastEntityEvent(golemEntity, EVENT_EMOTE_TASK);
                }
                break;
            }
        }
        return true;
    }

    protected @Nullable ItemEntity getItemEntity(Level level, Task task) {
        Integer entityId = itemEntities.get(task.getId());
        if (entityId != null && level.getEntity(entityId) instanceof ItemEntity item) {
            return item;
        }
        return null;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        ItemEntity item = getItemEntity(golem.getGolemWorld(), task);
        if (item == null || item.getItem().isEmpty()) {
            return false;
        }
        if (!item.isAlive()) {
            task.setSuspended(true);
            return false;
        }
        return golem.canCarry(item.getItem(), true);
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return !level.getBlockState(pos).isAir();
    }

    @Override
    public Identifier getSealIcon() {
        return TCIds.rl("textures/item/seal_pickup.png");
    }

    @Override
    public int[] getGuiCategories() {
        return new int[]{CAT_AREA, CAT_FILTER, CAT_PRIORITY, CAT_TAGS};
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
    public void onTaskSuspension(Level level, Task task) {}

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {}
}
