package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class BlockEntityItemGrate extends BlockEntity {
    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return canEject();
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            eject();
        }
    };

    public BlockEntityItemGrate(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ITEM_GRATE.get(), pos, state);
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    private boolean canEject() {
        if (level == null || level.isClientSide() || !getBlockState().getValue(BlockItemGrate.OPEN)) return false;
        BlockPos below = worldPosition.below();
        BlockState state = level.getBlockState(below);
        return state.is(TTBlocks.INFERNAL_FURNACE.get()) || !state.isSolidRender(level, below);
    }

    public void eject() {
        if (!canEject()) return;
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.isEmpty()) return;
        inventory.setStackInSlot(0, ItemStack.EMPTY);
        ItemEntity item = new ItemEntity(
                level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.625, worldPosition.getZ() + 0.5, stack);
        item.setDeltaMovement(0.0, -0.1, 0.0);
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
        setChanged();
    }

    public void dropContents() {
        if (level == null || level.isClientSide()) return;
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.isEmpty()) return;
        inventory.setStackInSlot(0, ItemStack.EMPTY);
        Containers.dropItemStack(
                level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, stack);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        if (input.contains("Inventory")) {
            inventory.deserializeNBT(registries, input.getCompound("Inventory"));
        }
    }
}
