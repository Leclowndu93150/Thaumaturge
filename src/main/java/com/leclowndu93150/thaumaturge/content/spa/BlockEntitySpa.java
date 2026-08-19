package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public class BlockEntitySpa extends BlockEntity implements MenuProvider {
    public static final int TANK_CAPACITY = 5000;
    private static final int WORK_INTERVAL_TICKS = 40;
    private static final int FLUID_COST = 1000;
    private static final int SPREAD_RADIUS = 2;

    private final FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1, TANK_CAPACITY) {
        @Override
        protected void onContentsChanged(int index, FluidStack previousContents) {
            super.onContentsChanged(index, previousContents);
            setChanged();
            syncToClient();
        }
    };

    private final ItemStacksResourceHandler items = new ItemStacksResourceHandler(1) {
        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.is(TCItems.BATH_SALTS.get());
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            super.onContentsChanged(index, previousContents);
            setChanged();
        }
    };

    private boolean mix = true;
    private int counter;

    public BlockEntitySpa(BlockPos pos, BlockState state) {
        super(TCBlockEntities.SPA.get(), pos, state);
    }

    public FluidStacksResourceHandler getTank() {
        return tank;
    }

    public ItemStacksResourceHandler getItems() {
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
            return TCBlocks.PURIFYING_FLUID.get();
        }
        Fluid fluid = tank.getResource(0).getFluid();
        if (!(fluid instanceof FlowingFluid)) {
            return null;
        }
        Block block = fluid.defaultFluidState().createLegacyBlock().getBlock();
        return block == Blocks.AIR ? null : block;
    }

    private boolean hasIngredients() {
        if (mix) {
            return tank.getResource(0).is(Fluids.WATER) && tank.getAmountAsInt(0) >= FLUID_COST && items.getResource(0).is(TCItems.BATH_SALTS.get());
        }
        return tank.getAmountAsInt(0) >= FLUID_COST && targetBlock() != null;
    }

    private void consumeIngredients() {
        try (Transaction ctx = Transaction.openRoot()) {
            if (mix) {
                items.extract(items.getResource(0), 1, ctx);
            }
            tank.extract(tank.getResource(0), FLUID_COST, ctx);
            ctx.commit();
        }
    }

    private boolean isValidLocation(BlockPos pos, boolean mustBeAdjacent, Block target) {
        Fluid fluid = target.defaultBlockState().getFluidState().getType();
        if (fluid.getFluidType().isVaporizedOnPlacement(level, pos, new FluidStack(fluid, FLUID_COST))) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        BlockState below = level.getBlockState(pos.below());
        if (!below.isFaceSturdy(level, pos.below(), Direction.UP) || !state.canBeReplaced() || (state.getBlock() == target && state.getFluidState().isSource())) {
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

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Mix", mix);
        tank.serialize(output.child("Tank"));
        items.serialize(output.child("Items"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        mix = input.getBooleanOr("Mix", true);
        input.child("Tank").ifPresent(tank::deserialize);
        input.child("Items").ifPresent(items::deserialize);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput out = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(out);
            nbt.merge(out.buildResult());
        }
        return nbt;
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
