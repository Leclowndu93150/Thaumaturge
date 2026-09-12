package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class BlockEntityItemGrate extends BlockEntity {
    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            eject();
        }
    };

    public BlockEntityItemGrate(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ITEM_GRATE.get(), pos, state);
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public void eject() {
        if (level == null || level.isClientSide() || !getBlockState().getValue(BlockItemGrate.OPEN)) return;
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.isEmpty()) return;
        inventory.setStackInSlot(0, ItemStack.EMPTY);
        Containers.dropItemStack(
                level, worldPosition.getX() + 0.5, worldPosition.getY() - 0.15, worldPosition.getZ() + 0.5, stack);
        setChanged();
    }
}
