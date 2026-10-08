package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jspecify.annotations.Nullable;

public class BlockEntitySpa extends AbstractSyncedBlockEntity implements MenuProvider {
    public static final int TANK_CAPACITY = 5000;
    private static final int WORK_INTERVAL_TICKS = 40;
    private static final int FLUID_COST = 1000;
    private static final int SPREAD_RADIUS = 2;

    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            syncToClient();
        }
    };

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int index, ItemStack resource) {
            return resource.is(TTItems.BATH_SALTS.get());
        }

        @Override
        protected void onContentsChanged(int index) {
            setChanged();
        }
    };

    private boolean mix = true;
    private int counter;

    public BlockEntitySpa(BlockPos pos, BlockState state) {
        super(TTBlockEntities.SPA.get(), pos, state);
    }

    public FluidTank getTank() {
        return tank;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean getMix() {
        return mix;
    }

    public void toggleMix() {
        mix = !mix;
        setChanged();
        syncToClient();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntitySpa spa) {
        if (spa.counter++ % WORK_INTERVAL_TICKS != 0 || level.hasNeighborSignal(pos) || !spa.hasIngredients()) {
            return;
        }
        Block target = spa.targetBlock();
        if (target == null) {
            return;
        }
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.getBlock() == target && aboveState.getFluidState().isSource()) {
            for (int xx = -SPREAD_RADIUS; xx <= SPREAD_RADIUS; xx++) {
                for (int zz = -SPREAD_RADIUS; zz <= SPREAD_RADIUS; zz++) {
                    BlockPos p = pos.offset(xx, 1, zz);
                    if (spa.isValidLocation(p, true, target)) {
                        spa.consumeIngredients();
                        level.setBlockAndUpdate(p, target.defaultBlockState());
                        return;
                    }
                }
            }
        } else if (spa.isValidLocation(above, false, target)) {
            spa.consumeIngredients();
            level.setBlockAndUpdate(above, target.defaultBlockState());
        }
    }

    private @Nullable Block targetBlock() {
        if (mix) {
            return TTBlocks.PURIFYING_FLUID.get();
        }
        Fluid fluid = tank.getFluid().getFluid();
        if (!(fluid instanceof FlowingFluid)) {
            return null;
        }
        Block block = fluid.defaultFluidState().createLegacyBlock().getBlock();
        return block == Blocks.AIR ? null : block;
    }

    private boolean hasIngredients() {
        if (mix) {
            return tank.getFluid().is(Fluids.WATER)
                    && tank.getFluidAmount() >= FLUID_COST
                    && items.getStackInSlot(0).is(TTItems.BATH_SALTS.get());
        }
        return tank.getFluidAmount() >= FLUID_COST && targetBlock() != null;
    }

    private void consumeIngredients() {
        if (mix) {
            items.extractItem(0, 1, false);
        }
        tank.drain(FLUID_COST, IFluidHandler.FluidAction.EXECUTE);
    }

    private boolean isValidLocation(BlockPos pos, boolean mustBeAdjacent, Block target) {
        Fluid fluid = target.defaultBlockState().getFluidState().getType();
        if (fluid.getFluidType().isVaporizedOnPlacement(level, pos, new FluidStack(fluid, FLUID_COST))) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        BlockState below = level.getBlockState(pos.below());
        if (!below.isFaceSturdy(level, pos.below(), Direction.UP)
                || !state.canBeReplaced()
                || (state.getBlock() == target && state.getFluidState().isSource())) {
            return false;
        }
        if (!mustBeAdjacent) {
            return true;
        }
        for (Direction dir : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));
            if (neighbor.getBlock() == target && neighbor.getFluidState().isSource()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putBoolean("Mix", mix);
        output.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        output.put("Items", items.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        mix = (input.contains("Mix") ? input.getBoolean("Mix") : true);
        if (input.contains("Tank")) {
            tank.readFromNBT(registries, input.getCompound("Tank"));
        }
        if (input.contains("Items")) {
            items.deserializeNBT(registries, input.getCompound("Items"));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumaturge.spa");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MenuSpa(containerId, playerInventory, this);
    }
}
