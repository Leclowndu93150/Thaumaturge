package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public final class BlockEntityStabilizer extends AbstractSyncedBlockEntity {
    public static final int MAX_CHARGE = 15;

    private static final int CHARGE_INTERVAL = 20;
    private static final int PULSE_INTERVAL = 5;
    private static final float FLUX_PER_CHARGE = 0.25F;
    private static final double REACH = 8.0;
    private static final int COOLDOWN_PER_RIFT = 5;
    private static final String CHARGE_KEY = "energy";

    private int charge;
    private int cooldown;
    private int ticks;

    public BlockEntityStabilizer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.STABILIZER.get(), pos, state);
    }

    public int charge() {
        return charge;
    }

    public boolean spendForShield(int cost) {
        if (cost <= 0 || charge < cost) {
            return false;
        }
        changeCharge(charge - cost);
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityStabilizer stabilizer) {
        stabilizer.work(level);
    }

    private void work(Level level) {
        ticks++;
        if (cooldown > 0) {
            cooldown--;
        }
        if (ticks % CHARGE_INTERVAL == 0 && charge < MAX_CHARGE) {
            changeCharge(charge + 1);
            AuraHelper.polluteAura(level, worldPosition, FLUX_PER_CHARGE, true);
        }
        if (ticks % PULSE_INTERVAL == 0 && charge > 0 && cooldown <= 0) {
            steadyRifts(level);
        }
    }

    private void steadyRifts(Level level) {
        AABB reach = new AABB(worldPosition).inflate(REACH);
        int remaining = charge;
        for (EntityFluxRift rift : level.getEntitiesOfClass(EntityFluxRift.class, reach)) {
            if (remaining <= 0) {
                break;
            }
            if (rift.stabilityTier() == EntityFluxRift.Stability.VERY_STABLE) {
                continue;
            }
            remaining--;
            rift.nudgeStability();
            cooldown += COOLDOWN_PER_RIFT;
        }
        if (remaining != charge) {
            changeCharge(remaining);
        }
    }

    private void changeCharge(int next) {
        int clamped = Math.clamp(next, 0, MAX_CHARGE);
        if (clamped == charge) {
            return;
        }
        charge = clamped;
        setChangedAndSync();
        if (level != null && !level.isClientSide()) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            refreshInlays(level);
        }
    }

    private void refreshInlays(Level level) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = worldPosition.relative(side);
            if (level.hasChunkAt(neighbour) && BlockInlay.chargeAt(level, neighbour) >= 0) {
                BlockInlay.updateNetwork(level, neighbour);
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        charge = Math.clamp(input.getIntOr(CHARGE_KEY, 0), 0, MAX_CHARGE);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(CHARGE_KEY, charge);
    }
}
