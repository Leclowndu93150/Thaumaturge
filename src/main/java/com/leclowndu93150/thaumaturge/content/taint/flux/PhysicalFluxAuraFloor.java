package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public final class PhysicalFluxAuraFloor {
    private static final float ABSOLUTE_CAP = 30.0F;
    private static final float BASE_CAP_RATIO = 0.3F;

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

    public static float target(LevelChunk chunk, float auraBase) {
        if (!isEnabled()) {
            return 0.0F;
        }
        PhysicalFluxSamples samples = chunk.getExistingDataOrNull(TTAttachments.PHYSICAL_FLUX_SAMPLES.get());
        if (samples == null) {
            return 0.0F;
        }
        return Math.min(samples.auraFloor(chunk), Math.min(ABSOLUTE_CAP, Math.max(0.0F, auraBase) * BASE_CAP_RATIO));
    }
}
