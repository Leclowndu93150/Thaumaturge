package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISeal;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigArea;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealGui;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumaturge.content.golem.CropUtils;
import com.leclowndu93150.thaumaturge.content.golem.GolemInteractionHelper;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandler;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import com.leclowndu93150.thaumaturge.server.TCFakePlayer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

public class SealHarvest implements ISeal, ISealGui, ISealConfigArea, ISealConfigToggles {
    private static final int REPLANT_CLEAN_INTERVAL = 100;
    private static final int SCAN_INTERVAL = 5;
    private static final int LEVEL_EVENT_BLOCK_BREAK = 2001;
    private static final short REPLANT_LIFESPAN = 300;

    protected final ISealConfigToggles.SealToggle[] props = {new ISealConfigToggles.SealToggle(true, "prep", "golem.prop.replant"),
            new ISealConfigToggles.SealToggle(false, "ppro", "golem.prop.provision")};

    private int delay = System.identityHashCode(this) % 33;
    private int count;
    private final Map<Long, ReplantInfo> replantTasks = new HashMap<>();

    @Override
    public Identifier getKey() {
        return TCIds.rl("harvest");
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay % REPLANT_CLEAN_INTERVAL == 0) {
            AABB area = GolemHelper.getBoundsForArea(seal);
            Iterator<Long> iterator = replantTasks.keySet().iterator();
            while (iterator.hasNext()) {
                BlockPos pos = BlockPos.of(iterator.next());
                if (!area.contains(Vec3.atCenterOf(pos))) {
                    ReplantInfo info = replantTasks.get(pos.asLong());
                    if (info != null) {
                        Task task = TaskHandler.getTask(level, info.taskId);
                        if (task != null) {
                            task.setSuspended(true);
                        }
                    }
                    iterator.remove();
                }
            }
        }
        if (delay++ % SCAN_INTERVAL != 0) {
            return;
        }
        BlockPos pos = GolemHelper.getPosInArea(seal, count++);
        if (CropUtils.isGrownCrop(level, pos)) {
            Task task = new Task(seal.getSealPos(), pos);
            task.setPriority(seal.getPriority());
            TaskHandler.addTask(level, task);
        } else if (props[0].getValue() && replantTasks.containsKey(pos.asLong()) && level.getBlockState(pos).isAir()) {
            ReplantInfo info = replantTasks.get(pos.asLong());
            if (TaskHandler.getTask(level, info.taskId) == null) {
                Task task = new Task(seal.getSealPos(), info.pos);
                task.setPriority(seal.getPriority());
                TaskHandler.addTask(level, task);
                info.taskId = task.getId();
            }
        }
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        if (!(level instanceof ServerLevel serverLevel)) {
            task.setSuspended(true);
            return true;
        }
        if (CropUtils.isGrownCrop(level, task.getPos())) {
            harvestCrop(serverLevel, golem, task);
        } else {
            replantCrop(serverLevel, golem, task);
        }
        task.setSuspended(true);
        return true;
    }

    private void harvestCrop(ServerLevel level, IGolemAPI golem, Task task) {
        FakePlayer player = TCFakePlayer.GOLEM.at(level, golem.getGolemEntity());
        BlockState state = level.getBlockState(task.getPos());
        if (CropUtils.isClickableCrop(state)) {
            Direction face = Direction.getApproximateNearest(task.getPos().getX() + 0.5 - golem.getGolemEntity().getX(), task.getPos().getY() + 0.5 - golem.getGolemEntity().getY(),
                    task.getPos().getZ() + 0.5 - golem.getGolemEntity().getZ()).getOpposite();
            state.useWithoutItem(level, player, new BlockHitResult(Vec3.atCenterOf(task.getPos()), face, task.getPos(), false));
            golem.addRankXp(1);
            golem.swingArm();
            return;
        }
        GolemInteractionHelper.golemClick(level, golem, task.getPos(), task.getSealPos().face(), ItemStack.EMPTY, false, true);
        if (!CropUtils.isGrownCrop(level, task.getPos())) {
            return;
        }
        ItemStack seed = CropUtils.getSeed(level, task.getPos(), state);
        BlockBreakerEngine.harvestBlock(level, player, task.getPos(), false, 0);
        golem.addRankXp(1);
        golem.swingArm();
        if (!props[0].getValue() || seed.isEmpty()) {
            return;
        }
        BlockState below = level.getBlockState(task.getPos().below());
        Direction replantFace = null;
        boolean cocoa = state.getBlock() instanceof CocoaBlock;
        if (!cocoa && below.is(BlockTags.DIRT) || below.getBlock() instanceof FarmlandBlock) {
            replantFace = Direction.DOWN;
        } else if (cocoa) {
            replantFace = state.getValue(CocoaBlock.FACING);
        }
        if (replantFace != null) {
            Task replant = new Task(task.getSealPos(), task.getPos());
            replant.setPriority(task.getPriority());
            replant.setLifespan(REPLANT_LIFESPAN);
            TaskHandler.addTask(level, replant);
            replantTasks.put(replant.getPos().asLong(), new ReplantInfo(replant.getPos(), replantFace, replant.getId(), seed.copy(), below.getBlock() instanceof FarmlandBlock));
        }
    }

    private void replantCrop(ServerLevel level, IGolemAPI golem, Task task) {
        ReplantInfo info = replantTasks.get(task.getPos().asLong());
        if (info == null || info.taskId != task.getId() || !level.getBlockState(task.getPos()).isAir() || !golem.isCarrying(info.stack)) {
            return;
        }
        FakePlayer player = TCFakePlayer.GOLEM.at(level, golem.getGolemEntity());
        BlockState below = level.getBlockState(task.getPos().below());
        if (info.farmland && below.is(BlockTags.DIRT) && !(below.getBlock() instanceof FarmlandBlock)) {
            ItemStack hoe = new ItemStack(Items.DIAMOND_HOE);
            player.setItemInHand(InteractionHand.MAIN_HAND, hoe);
            hoe.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(task.getPos().below()), Direction.UP, task.getPos().below(), false)));
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        ItemStack seed = info.stack.copy();
        seed.setCount(1);
        ItemStack planted = seed.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, seed);
        InteractionResult result = seed.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(task.getPos().relative(info.face)), info.face.getOpposite(), task.getPos().relative(info.face), false)));
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        if (result instanceof InteractionResult.Success) {
            level.globalLevelEvent(LEVEL_EVENT_BLOCK_BREAK, task.getPos(), Block.getId(level.getBlockState(task.getPos())));
            golem.dropItem(planted);
            golem.addRankXp(1);
            golem.swingArm();
        }
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        ReplantInfo info = replantTasks.get(task.getPos().asLong());
        if (info != null && info.taskId == task.getId()) {
            boolean carrying = golem.isCarrying(info.stack);
            if (!carrying && props[1].getValue()) {
                ISealEntity seal = SealHandler.getSealEntity(golem.getGolemWorld(), task.getSealPos());
                if (seal != null) {
                    GolemHelper.requestProvisioning(golem.getGolemWorld(), seal, info.stack);
                }
            }
            return carrying;
        }
        return true;
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {}

    @Override
    public void readCustomNBT(CompoundTag nbt) {
        replantTasks.clear();
        nbt.getList("replant").ifPresent(list -> {
            for (int i = 0; i < list.size(); i++) {
                list.getCompound(i).ifPresent(entry -> {
                    long loc = entry.getLongOr("taskloc", 0L);
                    byte face = entry.getByteOr("taskface", (byte) 0);
                    boolean farmland = entry.getBooleanOr("farmland", false);
                    Tag seedTag = entry.get("seed");
                    ItemStack stack = seedTag == null ? ItemStack.EMPTY : ItemStack.OPTIONAL_CODEC.parse(NbtOps.INSTANCE, seedTag).result().orElse(ItemStack.EMPTY);
                    replantTasks.put(loc, new ReplantInfo(BlockPos.of(loc), Direction.values()[face], 0, stack, farmland));
                });
            }
        });
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt) {
        if (!props[0].getValue()) {
            return;
        }
        ListTag list = new ListTag();
        for (ReplantInfo info : replantTasks.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("taskloc", info.pos.asLong());
            entry.putByte("taskface", (byte) info.face.ordinal());
            entry.putBoolean("farmland", info.farmland);
            Tag seed = ItemStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, info.stack).result().orElse(null);
            if (seed != null) {
                entry.put("seed", seed);
            }
            list.add(entry);
        }
        nbt.put("replant", list);
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return !level.getBlockState(pos).isAir();
    }

    @Override
    public Identifier getSealIcon() {
        return TCIds.rl("textures/item/seal_harvest.png");
    }

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {}

    @Override
    public int[] getGuiCategories() {
        return new int[]{CAT_AREA, CAT_TOGGLES, CAT_PRIORITY, CAT_TAGS};
    }

    @Override
    public ISealConfigToggles.SealToggle[] getToggles() {
        return props;
    }

    @Override
    public void setToggle(int index, boolean value) {
        props[index].setValue(value);
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
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {}

    private static final class ReplantInfo {
        private final Direction face;
        private final BlockPos pos;
        private int taskId;
        private final ItemStack stack;
        private final boolean farmland;

        private ReplantInfo(BlockPos pos, Direction face, int taskId, ItemStack stack, boolean farmland) {
            this.pos = pos;
            this.face = face;
            this.taskId = taskId;
            this.stack = stack;
            this.farmland = farmland;
        }
    }
}
