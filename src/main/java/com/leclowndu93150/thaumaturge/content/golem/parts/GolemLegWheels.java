package com.leclowndu93150.thaumaturge.content.golem.parts;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemLeg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public final class GolemLegWheels implements GolemLeg.ILegFunction {
    private static final double MIN_TRAVEL_FOR_DUST = 0.25;

    @Override
    public void onUpdateTick(IGolemAPI golem) {
        Level level = golem.getGolemWorld();
        if (!level.isClientSide()) {
            return;
        }
        LivingEntity entity = golem.getGolemEntity();
        double dist = Math.sqrt(entity.distanceToSqr(entity.xOld, entity.yOld, entity.zOld));
        if (!entity.onGround() || entity.isInWater() || dist <= MIN_TRAVEL_FOR_DUST) {
            return;
        }
        BlockPos below = new BlockPos(Mth.floor(entity.getX()), Mth.floor(entity.getY() - 0.2F), Mth.floor(entity.getZ()));
        BlockState state = level.getBlockState(below);
        if (state.getRenderShape() != RenderShape.INVISIBLE) {
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), entity.getX() + (level.getRandom().nextFloat() - 0.5) * entity.getBbWidth(), entity.getBoundingBox().minY + 0.1,
                    entity.getZ() + (level.getRandom().nextFloat() - 0.5) * entity.getBbWidth(), -entity.getDeltaMovement().x * 4.0, 1.5, -entity.getDeltaMovement().z * 4.0);
        }
    }
}
