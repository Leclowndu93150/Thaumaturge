package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.aura.IAuraChunk;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BlockEntityDioptra extends AbstractSyncedBlockEntity {
    public static final int GRID_SIZE = 13;
    public static final int GRID_LENGTH = GRID_SIZE * GRID_SIZE;
    public static final int STEPS = 64;

    private static final int GRID_RADIUS = GRID_SIZE / 2;
    private static final int CENTER_INDEX = GRID_RADIUS * GRID_SIZE + GRID_RADIUS;
    private static final int SAMPLE_INTERVAL = 20;
    private static final float FULL_READING = 500.0F;

    private final byte[] steps = new byte[GRID_LENGTH];
    private boolean sampledVis;
    private boolean sampledOnce;

    public BlockEntityDioptra(BlockPos pos, BlockState state) {
        super(TTBlockEntities.DIOPTRA.get(), pos, state);
    }

    public byte gridValue(int index) {
        return index >= 0 && index < GRID_LENGTH ? steps[index] : 0;
    }

    public void copyGrid(byte[] target) {
        System.arraycopy(steps, 0, target, 0, Math.min(target.length, GRID_LENGTH));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityDioptra dioptra) {
        boolean showVis = state.getValue(BlockStateProperties.ENABLED);
        boolean modeChanged = dioptra.sampledOnce && showVis != dioptra.sampledVis;
        if (Math.floorMod(level.getGameTime() + pos.asLong(), SAMPLE_INTERVAL) == 0
                || modeChanged
                || !dioptra.sampledOnce) {
            if (level instanceof ServerLevel serverLevel) {
                dioptra.sample(serverLevel, showVis);
            }
        }
    }

    private void sample(ServerLevel level, boolean showVis) {
        ChunkPos home = new ChunkPos(LevelSpaces.worldBlock(level, worldPosition));
        int centerBefore = steps[CENTER_INDEX];
        boolean changed = false;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int column = 0; column < GRID_SIZE; column++) {
                IAuraChunk aura =
                        AuraHelper.of(level, new ChunkPos(home.x + column - GRID_RADIUS, home.z + row - GRID_RADIUS));
                byte step = toStep(showVis ? aura.getVis() : aura.getFlux());
                int index = row * GRID_SIZE + column;
                if (steps[index] != step) {
                    steps[index] = step;
                    changed = true;
                }
            }
        }
        sampledVis = showVis;
        sampledOnce = true;
        if (!changed) {
            return;
        }
        setChangedAndSync();
        if (steps[CENTER_INDEX] != centerBefore) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    static byte toStep(float amount) {
        float fraction = Mth.clamp(amount / FULL_READING, 0.0F, 1.0F);
        return (byte) Math.round(fraction * STEPS);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        int[] saved = input.getIntArray("aura_steps");
        if (saved.length != GRID_LENGTH) {
            saved = input.getIntArray("grid_a");
        }
        if (saved.length == GRID_LENGTH) {
            for (int i = 0; i < GRID_LENGTH; i++) {
                steps[i] = (byte) Mth.clamp(saved[i], 0, STEPS);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        int[] out = new int[GRID_LENGTH];
        for (int i = 0; i < GRID_LENGTH; i++) {
            out[i] = steps[i];
        }
        // Keep the 1.21.1 key so already-saved Dioptras remain readable by older worlds/mod versions.
        output.putIntArray("grid_a", out);
    }
}
