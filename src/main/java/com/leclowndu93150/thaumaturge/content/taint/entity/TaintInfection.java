package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TCEntityTags;
import com.leclowndu93150.thaumaturge.registry.TCMobTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class TaintInfection {
    private static final float PROFILED_PRESSURE = 0.04F;
    private static final float GENERIC_PRESSURE = 0.01F;

    private TaintInfection() {}

    public static boolean canInfect(ServerLevel level, LivingEntity mob) {
        return !ThaumaturgeCommonConfig.WUSS_MODE.get()
                && level.getDifficulty() != Difficulty.PEACEFUL
                && mob instanceof Mob
                && !MobTraits.isTainted(mob)
                && !mob.getType().is(TCEntityTags.TAINT_CONVERSION_IMMUNE);
    }

    public static void tryInfect(ServerLevel level, LivingEntity mob) {
        if (canInfect(level, mob) && MobTraits.add(mob, TCMobTraits.TAINTED)) {
            TaintEcology.addPressure(
                    level,
                    mob.blockPosition(),
                    TaintedProfile.of(mob.getType()) == null ? GENERIC_PRESSURE : PROFILED_PRESSURE);
        }
    }
}
