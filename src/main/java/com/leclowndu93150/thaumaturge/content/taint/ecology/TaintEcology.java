package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Small boundary API for persistent ecological taint pressure. */
public final class TaintEcology {
    public static final float TAINTED_THRESHOLD = 0.2F;
    private static final float SEED_PRESSURE_BASE = 0.002F;
    private static final float SEED_PRESSURE_FROM_FLUX = 0.003F;

    private TaintEcology() {}

    public static float getSaturation(ServerLevel level, BlockPos pos) {
        float flux = Math.min(1.0F, Math.max(0.0F, AuraHelper.getFluxSaturation(level, pos)));
        float decayMultiplier = 1.0F - flux * 0.75F;
        return TaintEcologyState.get(level).getSaturation(pos, level.getGameTime(), decayMultiplier);
    }

    public static boolean isTainted(ServerLevel level, BlockPos pos) {
        return TaintBiomeManager.isTainted(level, pos) || getSaturation(level, pos) >= TAINTED_THRESHOLD;
    }

    public static float addPressure(ServerLevel level, BlockPos pos, float amount) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return getSaturation(level, pos);
        }
        return TaintEcologyState.get(level).addPressure(pos, amount, level.getGameTime(), false);
    }

    public static float clean(ServerLevel level, BlockPos pos, float amount) {
        return TaintEcologyState.get(level).clean(pos, amount, level.getGameTime());
    }

    public static void setSaturation(ServerLevel level, BlockPos pos, float saturation) {
        TaintEcologyState.get(level).setSaturation(pos, saturation, level.getGameTime());
    }

    public static void touchActiveSeed(ServerLevel level, BlockPos pos) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return;
        }
        float fluxContribution = Math.min(1.0F, Math.max(0.0F, AuraHelper.getFluxSaturation(level, pos)));
        float pressure = SEED_PRESSURE_BASE + fluxContribution * SEED_PRESSURE_FROM_FLUX;
        TaintEcologyState.get(level).addPressure(pos, pressure, level.getGameTime(), true);
    }
}
