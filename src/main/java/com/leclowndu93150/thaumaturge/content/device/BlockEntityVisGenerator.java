package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class BlockEntityVisGenerator extends BlockEntity implements IEnergyStorage {
    private static final int CAPACITY = 1000;
    private static final int MAX_PUSH = 20;
    private static final float VIS_PER_CHARGE = 1.0F;
    private static final int ENERGY_PER_VIS = 1000;

    private int energy;

    public BlockEntityVisGenerator(BlockPos pos, BlockState state) {
        super(TTBlockEntities.VIS_GENERATOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityVisGenerator generator) {
        if (!state.getValue(BlockStateProperties.ENABLED)) {
            return;
        }
        if (generator.energy == 0) {
            float vis = AuraHelper.drainVis(level, pos, VIS_PER_CHARGE, false);
            generator.energy = (int) (vis * ENERGY_PER_VIS);
            generator.setChanged();
        }
        Direction facing = state.getValue(BlockStateProperties.FACING);
        IEnergyStorage target =
                level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(facing), facing.getOpposite());
        if (target != null) {
            int pushed = target.receiveEnergy(Math.min(generator.energy, MAX_PUSH), false);
            if (pushed > 0) {
                generator.energy -= pushed;
                generator.setChanged();
            }
        }
    }

    public Direction outputFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return CAPACITY;
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int amount, boolean simulate) {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        energy = input.getInt("energy");
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putInt("energy", energy);
    }
}
