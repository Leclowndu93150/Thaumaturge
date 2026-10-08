package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.aura.IAuraChunk;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BlockEntityDioptra extends AbstractSyncedBlockEntity {
    public static final int GRID_SIZE = 13;
    public static final int GRID_LENGTH = GRID_SIZE * GRID_SIZE;
    private static final int SAMPLE_INTERVAL = 20;
    private static final int CHUNK_OFFSET = 6;
    private static final float AURA_SCALE = 500.0F;
    private static final float GRID_MAX = 64.0F;

    private byte[] grid = new byte[GRID_LENGTH];
    private int counter;

    public BlockEntityDioptra(BlockPos pos, BlockState state) {
        super(TTBlockEntities.DIOPTRA.get(), pos, state);
    }

    public byte gridValue(int index) {
        return index >= 0 && index < grid.length ? grid[index] : 0;
    }

    public byte[] grid() {
        return grid;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityDioptra dioptra) {
        dioptra.counter++;
        if (dioptra.counter % SAMPLE_INTERVAL != 0 || !(level instanceof ServerLevel server)) {
            return;
        }
        boolean changed = false;
        boolean enabled = state.getValue(BlockStateProperties.ENABLED);
        int baseChunkX = pos.getX() >> 4;
        int baseChunkZ = pos.getZ() >> 4;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = 0; xx < GRID_SIZE; xx++) {
            for (int zz = 0; zz < GRID_SIZE; zz++) {
                cursor.set((baseChunkX + xx - CHUNK_OFFSET) << 4, 0, (baseChunkZ + zz - CHUNK_OFFSET) << 4);
                IAuraChunk aura = AuraHelper.of(server, cursor);
                float value = enabled ? aura.getVis() : aura.getFlux();
                byte sample = (byte) Math.min(GRID_MAX, value / AURA_SCALE * GRID_MAX);
                int index = xx + zz * GRID_SIZE;
                if (dioptra.grid[index] != sample) {
                    dioptra.grid[index] = sample;
                    changed = true;
                }
            }
        }
        if (changed) {
            dioptra.setChanged();
            server.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        int[] read = input.contains("grid_a") ? input.getIntArray("grid_a") : null;
        if (read != null && read.length == GRID_LENGTH) {
            for (int i = 0; i < GRID_LENGTH; i++) {
                grid[i] = (byte) read[i];
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        int[] out = new int[GRID_LENGTH];
        for (int i = 0; i < GRID_LENGTH; i++) {
            out[i] = grid[i];
        }
        output.putIntArray("grid_a", out);
    }
}
