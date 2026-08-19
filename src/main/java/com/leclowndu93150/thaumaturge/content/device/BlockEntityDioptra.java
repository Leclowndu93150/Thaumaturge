package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.aura.IAuraChunk;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BlockEntityDioptra extends BlockEntity {
    public static final int GRID_SIZE = 13;
    public static final int GRID_LENGTH = GRID_SIZE * GRID_SIZE;
    private static final int SAMPLE_INTERVAL = 20;
    private static final int CHUNK_OFFSET = 6;
    private static final float AURA_SCALE = 500.0F;
    private static final float GRID_MAX = 64.0F;

    private byte[] grid = new byte[GRID_LENGTH];
    private int counter;

    public BlockEntityDioptra(BlockPos pos, BlockState state) {
        super(TCBlockEntities.DIOPTRA.get(), pos, state);
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
        Arrays.fill(dioptra.grid, (byte) 0);
        boolean enabled = state.getValue(BlockStateProperties.ENABLED);
        int baseChunkX = pos.getX() >> 4;
        int baseChunkZ = pos.getZ() >> 4;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = 0; xx < GRID_SIZE; xx++) {
            for (int zz = 0; zz < GRID_SIZE; zz++) {
                cursor.set((baseChunkX + xx - CHUNK_OFFSET) << 4, 0, (baseChunkZ + zz - CHUNK_OFFSET) << 4);
                IAuraChunk aura = AuraHelper.of(server, cursor);
                float value = enabled ? aura.getVis() : aura.getFlux();
                dioptra.grid[xx + zz * GRID_SIZE] = (byte) Math.min(GRID_MAX, value / AURA_SCALE * GRID_MAX);
            }
        }
        dioptra.setChanged();
        server.sendBlockUpdated(pos, state, state, 3);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int[] read = input.getIntArray("grid_a").orElse(null);
        if (read != null && read.length == GRID_LENGTH) {
            for (int i = 0; i < GRID_LENGTH; i++) {
                grid[i] = (byte) read[i];
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        int[] out = new int[GRID_LENGTH];
        for (int i = 0; i < GRID_LENGTH; i++) {
            out[i] = grid[i];
        }
        output.putIntArray("grid_a", out);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
