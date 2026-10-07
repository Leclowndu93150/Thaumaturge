package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class BlockEntityStabilizer extends BlockEntity {
    private static final int CAPACITY = 15;
    private static final int CHARGE_INTERVAL = 20;
    private static final int WORK_INTERVAL = 5;
    private static final float POLLUTION_PER_CHARGE = 0.25F;
    private static final double RIFT_RANGE = 8.0;
    private static final int WORK_DELAY = 5;

    private int ticks;
    private int delay;
    private int energy;

    public BlockEntityStabilizer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.STABILIZER.get(), pos, state);
    }

    public int getEnergy() {
        return energy;
    }

    public boolean mitigate(int amount) {
        if (energy >= amount) {
            energy -= amount;
            setChanged();
            if (level != null) {
                level.updateNeighbourForOutputSignal(
                        getBlockPos(), getBlockState().getBlock());
            }
            return true;
        }
        return false;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityStabilizer stabilizer) {
        stabilizer.ticks++;
        if (stabilizer.energy < CAPACITY && stabilizer.ticks % CHARGE_INTERVAL == 0) {
            stabilizer.energy++;
            AuraHelper.polluteAura(level, pos, POLLUTION_PER_CHARGE, true);
            stabilizer.setChanged();
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        if (stabilizer.energy > 0 && stabilizer.delay <= 0 && stabilizer.ticks % WORK_INTERVAL == 0) {
            stabilizer.tryAddStability(level, pos);
        }
        if (stabilizer.delay > 0) {
            stabilizer.delay--;
        }
    }

    private void tryAddStability(Level level, BlockPos pos) {
        List<EntityFluxRift> rifts = level.getEntitiesOfClass(
                EntityFluxRift.class,
                new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
                        .inflate(RIFT_RANGE));
        for (EntityFluxRift rift : rifts) {
            if (!rift.isRemoved() && rift.getStability() != EntityFluxRift.Stability.VERY_STABLE && mitigate(1)) {
                rift.addStability();
                delay += WORK_DELAY;
                if (energy <= 0) {
                    return;
                }
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        energy = Math.min(input.getInt("energy"), CAPACITY);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putInt("energy", energy);
    }
}
