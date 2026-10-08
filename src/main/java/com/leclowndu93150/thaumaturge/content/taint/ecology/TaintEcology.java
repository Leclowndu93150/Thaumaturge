package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

public final class TaintEcology {
    public static final float TAINTED_THRESHOLD = 0.2F;
    private static final float MINIMUM_SATURATION = 0.0001F;
    private static final float DECAY_PER_TICK = 0.05F / 24000.0F;
    private static final float FLUX_DECAY_DAMPING = 0.75F;
    private static final float SEED_PRESSURE_BASE = 0.002F;
    private static final float SEED_PRESSURE_FROM_FLUX = 0.003F;

    private TaintEcology() {}

    public static float getSaturation(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = loadedChunk(level, pos);
        TaintPressure pressure = chunk == null ? null : chunk.getExistingDataOrNull(TTAttachments.TAINT_PRESSURE.get());
        if (pressure == null) {
            return 0.0F;
        }
        float saturation = pressure.saturationAt(level.getGameTime(), decayRate(level, pos));
        return saturation < MINIMUM_SATURATION ? 0.0F : saturation;
    }

    public static boolean isTainted(ServerLevel level, BlockPos pos) {
        return level.getBiome(pos).is(TTBiomeTags.IS_TAINTED) || getSaturation(level, pos) >= TAINTED_THRESHOLD;
    }

    public static float addPressure(ServerLevel level, BlockPos pos, float amount) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get() || amount <= 0.0F) {
            return getSaturation(level, pos);
        }
        return change(level, pos, amount, false);
    }

    public static float clean(ServerLevel level, BlockPos pos, float amount) {
        if (amount <= 0.0F) {
            return getSaturation(level, pos);
        }
        return change(level, pos, -amount, false);
    }

    public static void setSaturation(ServerLevel level, BlockPos pos, float saturation) {
        LevelChunk chunk = loadedChunk(level, pos);
        if (chunk != null) {
            store(chunk, level.getGameTime(), Mth.clamp(saturation, 0.0F, 1.0F), false);
        }
    }

    public static void touchActiveSeed(ServerLevel level, BlockPos pos) {
        if (!ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            change(level, pos, SEED_PRESSURE_BASE + fluxSaturation(level, pos) * SEED_PRESSURE_FROM_FLUX, true);
        }
    }

    private static float change(ServerLevel level, BlockPos pos, float delta, boolean activeSeed) {
        LevelChunk chunk = loadedChunk(level, pos);
        if (chunk == null) {
            return 0.0F;
        }
        long now = level.getGameTime();
        TaintPressure existing = chunk.getExistingDataOrNull(TTAttachments.TAINT_PRESSURE.get());
        float current = existing == null ? 0.0F : existing.saturationAt(now, decayRate(level, pos));
        return store(chunk, now, Mth.clamp(current + delta, 0.0F, 1.0F), activeSeed);
    }

    private static float store(LevelChunk chunk, long gameTime, float saturation, boolean activeSeed) {
        if (saturation < MINIMUM_SATURATION) {
            if (chunk.getExistingDataOrNull(TTAttachments.TAINT_PRESSURE.get()) != null) {
                chunk.removeData(TTAttachments.TAINT_PRESSURE.get());
                chunk.setUnsaved(true);
            }
            return 0.0F;
        }
        TaintPressure pressure = chunk.getData(TTAttachments.TAINT_PRESSURE.get());
        pressure.set(saturation, gameTime);
        if (activeSeed) {
            pressure.markActiveSeed(gameTime);
        }
        chunk.setUnsaved(true);
        return saturation;
    }

    private static float decayRate(ServerLevel level, BlockPos pos) {
        return DECAY_PER_TICK * (1.0F - fluxSaturation(level, pos) * FLUX_DECAY_DAMPING);
    }

    private static float fluxSaturation(ServerLevel level, BlockPos pos) {
        return Mth.clamp(AuraHelper.getFluxSaturation(level, pos), 0.0F, 1.0F);
    }

    private static @Nullable LevelChunk loadedChunk(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = level.getChunkSource()
                .getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        if (chunk != null) TaintLegacyData.importChunk(level, chunk);
        return chunk;
    }
}
