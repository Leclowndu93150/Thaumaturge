package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class PhysicalFluxAuraFloor {
    private static final float ABSOLUTE_CAP = 30.0F;
    private static final float BASE_CAP_RATIO = 0.3F;
    private static final int CONVERSION_INTERVAL_TICKS = 200;

    private PhysicalFluxAuraFloor() {}

    public static boolean isEnabled() {
        return ThaumaturgeCommonConfig.PHYSICAL_FLUX_AURA_FLOOR.get();
    }

    public static void observe(ServerLevel level, BlockPos pos) {
        if (!isEnabled() && !PhysicalFluxOutbreaks.isEnabled()) {
            return;
        }
        LevelChunk chunk = level.getChunkSource()
                .getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        if (chunk != null) {
            chunk.getData(TTAttachments.PHYSICAL_FLUX_SAMPLES.get()).add(pos);
        }
    }

    public static float replenish(ServerLevel level, LevelChunk chunk, float auraBase, float currentFlux) {
        if (level.getGameTime() % CONVERSION_INTERVAL_TICKS != 0 || !isEnabled()) {
            return 0.0F;
        }
        PhysicalFluxSamples samples = chunk.getExistingDataOrNull(TTAttachments.PHYSICAL_FLUX_SAMPLES.get());
        if (samples == null) {
            return 0.0F;
        }
        float gap = Math.min(samples.auraFloor(chunk), Math.min(ABSOLUTE_CAP, auraBase * BASE_CAP_RATIO)) - currentFlux;
        if (gap <= 0.0F) {
            return 0.0F;
        }
        for (BlockPos pos : samples.activePositions(chunk)) {
            BlockState state = chunk.getBlockState(pos);
            if (state.getBlock() instanceof PhysicalFluxBlock flux) {
                float yield = flux.auraFloorPerQuantum();
                if (yield > 0.0F && yield <= gap && PhysicalFlux.reduce(level, pos, 1) == 1) {
                    return yield;
                }
            }
        }
        return 0.0F;
    }
}
