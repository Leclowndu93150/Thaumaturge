package com.leclowndu93150.thaumaturge.content.warp.effect;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class ThaumarhiaEffect extends MobEffect {
    private static final int TICK_INTERVAL = 20;
    private static final int GOO_CHANCE = 15;

    public ThaumarhiaEffect() {
        super(MobEffectCategory.HARMFUL, 0xB300B3);
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
        BlockPos pos = mob.blockPosition();
        if (level.getRandom().nextInt(GOO_CHANCE) == 0 && level.isEmptyBlock(pos)) {
            level.setBlockAndUpdate(pos, TCBlocks.FLUX_GOO.get().defaultBlockState());
        }
        return true;
    }
}
