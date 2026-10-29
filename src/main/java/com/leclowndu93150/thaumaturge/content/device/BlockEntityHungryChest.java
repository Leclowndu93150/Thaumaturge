package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class BlockEntityHungryChest extends ChestBlockEntity {
    private static final double HORIZONTAL_GROWTH = 0.1;
    private static final double VERTICAL_REACH = 0.3;
    private static final int LID_EVENT = 1;
    private static final int LID_OPEN = 1;
    private static final int LID_CLOSED = 0;
    private static final int LID_CLOSE_DELAY = 4;
    private static final float EAT_VOLUME = 0.25F;
    private static final float EAT_PITCH_SPREAD = 0.2F;
    private static final float EAT_PITCH_BASE = 1.0F;

    private final AABB swallowBox;
    private int lidCloseTimer;

    public BlockEntityHungryChest(BlockPos pos, BlockState state) {
        super(TTBlockEntities.HUNGRY_CHEST.get(), pos, state);
        swallowBox = new AABB(pos)
                .inflate(HORIZONTAL_GROWTH, 0.0, HORIZONTAL_GROWTH)
                .expandTowards(0.0, VERTICAL_REACH, 0.0);
    }

    @Override
    protected Component getDefaultName() {
        return TTBlocks.HUNGRY_CHEST.get().getName();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityHungryChest chest) {
        if (chest.lidCloseTimer > 0 && --chest.lidCloseTimer == 0) {
            level.blockEvent(pos, state.getBlock(), LID_EVENT, LID_CLOSED);
        }
        boolean swallowed = false;
        RandomSource random = level.getRandom();
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, chest.swallowBox)) {
            if (!item.isRemoved() && chest.swallow(level, pos, item, random)) {
                swallowed = true;
            }
        }
        if (swallowed) {
            chest.setChanged();
            chest.lidCloseTimer = LID_CLOSE_DELAY;
            level.blockEvent(pos, state.getBlock(), LID_EVENT, LID_OPEN);
        }
    }

    private boolean swallow(Level level, BlockPos pos, ItemEntity item, RandomSource random) {
        ItemStack stack = item.getItem();
        int remaining =
                InvHelper.insertAt(level, pos, Direction.UP, stack, false).getCount();
        int accepted = stack.getCount() - remaining;
        if (accepted <= 0) {
            return false;
        }
        if (remaining <= 0) {
            item.discard();
        } else {
            stack.shrink(accepted);
        }
        item.playSound(
                SoundEvents.GENERIC_EAT,
                EAT_VOLUME,
                (random.nextFloat() - random.nextFloat()) * EAT_PITCH_SPREAD + EAT_PITCH_BASE);
        return true;
    }
}
