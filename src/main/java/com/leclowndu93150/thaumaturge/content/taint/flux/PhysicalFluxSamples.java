package com.leclowndu93150.thaumaturge.content.taint.flux;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class PhysicalFluxSamples {
    private final LongSet positions = new LongOpenHashSet();
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

    public void add(BlockPos pos) {
        positions.add(pos.asLong());
    }

    public List<BlockPos> activePositions(LevelChunk chunk) {
        List<BlockPos> active = new ArrayList<>(positions.size());
        LongIterator iterator = positions.iterator();
        while (iterator.hasNext()) {
            long packed = iterator.nextLong();
            cursor.set(packed);
            if (chunk.getBlockState(cursor).getBlock() instanceof PhysicalFluxBlock) {
                active.add(BlockPos.of(packed));
            } else {
                iterator.remove();
            }
        }
        return active;
    }

    public float auraFloor(LevelChunk chunk) {
        float floor = 0.0F;
        LongIterator iterator = positions.iterator();
        while (iterator.hasNext()) {
            cursor.set(iterator.nextLong());
            BlockState state = chunk.getBlockState(cursor);
            if (state.getBlock() instanceof PhysicalFluxBlock flux) {
                floor += flux.fluxAmount(state) * flux.auraFloorPerQuantum();
            } else {
                iterator.remove();
            }
        }
        return floor;
    }
}
