package com.leclowndu93150.thaumaturge.content.taint.spread;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

public final class TaintFrontier {
    private static final int MIN_TAINT_NEIGHBOURS = 2;
    private static final int RATE_MULTIPLIER = 5;
    private static final float ACCELERATION_PER_SATURATION = 0.5F;
    private static final float MAX_ACCELERATION = 1.5F;
    private static final int COLUMN_REACH = 1;
    private static final float BASE_PRESSURE = 0.01F;
    private static final float PRESSURE_PER_SATURATION = 0.01F;
    private static final float MAX_PRESSURE_BONUS = 0.02F;

    private TaintFrontier() {}

    public static boolean tryAdvance(ServerLevel level, BlockPos pos, RandomSource random) {
        int rate = ThaumaturgeCommonConfig.TAINT_FRONTIER_RATE.get();
        if (rate <= 0 || !mayAdvanceFrom(level, pos)) {
            return false;
        }
        float saturation = Math.max(0.0F, AuraHelper.getFluxSaturation(level, pos));
        if (random.nextInt(oddsAgainst(rate, saturation)) != 0) {
            return false;
        }
        BlockPos column = pos.offset(columnOffset(random), 0, columnOffset(random));
        if (!TaintBiomeManager.taintColumn(level, column)) {
            return false;
        }
        TaintEcology.addPressure(level, column, BASE_PRESSURE + Math.min(MAX_PRESSURE_BONUS, saturation * PRESSURE_PER_SATURATION));
        return true;
    }

    private static boolean mayAdvanceFrom(ServerLevel level, BlockPos pos) {
        return !ThaumaturgeCommonConfig.WUSS_MODE.get() && TaintBiomeManager.isTainted(level, pos) && TaintHelper.countAdjacentTaint(level, pos) >= MIN_TAINT_NEIGHBOURS
                && !TaintBlooms.isProtected(level, pos);
    }

    private static int oddsAgainst(int rate, float saturation) {
        float acceleration = Math.min(MAX_ACCELERATION, 1.0F + saturation * ACCELERATION_PER_SATURATION);
        return Math.max(1, Math.round(RATE_MULTIPLIER * rate / acceleration));
    }

    private static int columnOffset(RandomSource random) {
        return random.nextInt(COLUMN_REACH * 2 + 1) - COLUMN_REACH;
    }
}
