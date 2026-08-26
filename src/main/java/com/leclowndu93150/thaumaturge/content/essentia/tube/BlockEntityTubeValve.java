package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityTubeValve extends BlockEntityTube {
    private static final float ROTATION_MAX = 360.0F;
    private static final float ROTATION_STEP = 20.0F;

    private boolean allowFlow = true;
    private boolean wasPoweredLastTick;
    private float previousRotation;
    private float rotation;

    public BlockEntityTubeValve(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE_VALVE.get(), pos, state);
    }

    public boolean allowFlow() {
        return allowFlow;
    }

    public float rotation() {
        return rotation;
    }

    public float rotation(float partialTick) {
        return previousRotation + (rotation - previousRotation) * partialTick;
    }

    public void setAllowFlow(boolean allow) {
        this.allowFlow = allow;
        if (!allow) {
            super.setSuction(null, 0);
        }
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void tickServer(Level level, BlockPos pos, BlockState state) {
        if (tickCount % 5 == 0) {
            boolean powered = level.hasNeighborSignal(pos);
            if (wasPoweredLastTick && !powered && !allowFlow) {
                allowFlow = true;
                level.playSound(
                        null,
                        pos,
                        TCSounds.SQUEEK.get(),
                        SoundSource.BLOCKS,
                        0.7F,
                        0.9F + level.getRandom().nextFloat() * 0.2F);
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            if (!wasPoweredLastTick && powered && allowFlow) {
                allowFlow = false;
                super.setSuction(null, 0);
                level.playSound(
                        null,
                        pos,
                        TCSounds.SQUEEK.get(),
                        SoundSource.BLOCKS,
                        0.7F,
                        0.9F + level.getRandom().nextFloat() * 0.2F);
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            wasPoweredLastTick = powered;
        }
        super.tickServer(level, pos, state);
    }

    public void tickClient(Level level, BlockPos pos, BlockState state) {
        previousRotation = rotation;
        if (!allowFlow && rotation < ROTATION_MAX) {
            rotation += ROTATION_STEP;
        } else if (allowFlow && rotation > 0.0F) {
            rotation -= ROTATION_STEP;
        }
    }

    @Override
    public boolean rotateFacing() {
        if (level == null) return false;
        int start = facing.ordinal();
        for (int offset = 1; offset < Direction.values().length; offset++) {
            Direction candidate = Direction.values()[(start + offset) % Direction.values().length];
            if (hasTransportNeighbour(candidate)) continue;
            facing = candidate;
            setChanged();
            pushUpdate(this);
            BlockEssentiaTransport.refreshConnectionsAround(level, getBlockPos());
            return true;
        }
        return false;
    }

    @Override
    public boolean isConnectable(Direction face) {
        if (face == null) return false;
        Direction f = facing();
        if (f != null && face == f) return false;
        return super.isConnectable(face);
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!allowFlow) return 0;
        return super.addEssentia(aspect, amount, face);
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!allowFlow) return 0;
        return super.takeEssentia(aspect, amount, face);
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {
        if (allowFlow || amount <= 0) {
            super.setSuction(aspect, amount);
        }
    }

    @Override
    public Holder<IAspect> getSuctionType(Direction face) {
        return allowFlow ? super.getSuctionType(face) : null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return allowFlow ? super.getSuctionAmount(face) : 0;
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        allowFlow = (input.contains("AllowFlow") ? input.getBoolean("AllowFlow") : true);
        wasPoweredLastTick = input.getBoolean("HadPower");
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putBoolean("AllowFlow", allowFlow);
        output.putBoolean("HadPower", wasPoweredLastTick);
    }
}
