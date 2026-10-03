package com.leclowndu93150.thaumaturge.content.warp.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class SunScornedEffect extends MobEffect {
    private static final int TICK_INTERVAL = 40;
    private static final float BURN_BRIGHTNESS = 0.5F;
    private static final float HEAL_BRIGHTNESS = 0.25F;
    private static final float BURN_SECONDS = 4.0F;
    private static final float HEAL_AMOUNT = 1.0F;

    public SunScornedEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFDD55);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return tickCount % TICK_INTERVAL == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity mob, int amplification) {
        if (mob.level().isClientSide()) {
            return true;
        }

        ServerLevel level = (ServerLevel) mob.level();
        BlockPos lightPos = BlockPos.containing(mob.getX(), mob.getEyeY(), mob.getZ());
        float brightness = level.hasChunk(
                        SectionPos.blockToSectionCoord(lightPos.getX()),
                        SectionPos.blockToSectionCoord(lightPos.getZ()))
                ? lightLevelDependentMagicValue(level, lightPos)
                : 0.0F;
        BlockPos pos = BlockPos.containing(mob.getX(), mob.getY(), mob.getZ());
        if (brightness > BURN_BRIGHTNESS
                && level.getRandom().nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F
                && level.canSeeSky(pos)) {
            mob.igniteForSeconds(BURN_SECONDS);
        } else if (brightness < HEAL_BRIGHTNESS && level.getRandom().nextFloat() > brightness * 2.0F) {
            mob.heal(HEAL_AMOUNT);
        }
        return true;
    }

    private static float lightLevelDependentMagicValue(ServerLevel level, BlockPos pos) {
        float brightness = level.getMaxLocalRawBrightness(pos) / 15.0F;
        float adjustedBrightness = 4.0F / (4.0F - 3.0F * brightness);
        return Mth.lerp(level.dimensionType().ambientLight(), adjustedBrightness, 1.0F);
    }
}
