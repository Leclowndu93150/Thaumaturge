package com.leclowndu93150.thaumaturge.content.golem.parts;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.parts.IGolemPartAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class RollerLegDust implements IGolemPartAbility {
    private static final double BRISK_SPEED = 0.25D;
    private static final double BRISK_SPEED_SQR = BRISK_SPEED * BRISK_SPEED;
    private static final double GROUND_PROBE = 0.05D;
    private static final double BASE_LIFT = 0.1D;
    private static final double CENTER = 0.5D;
    private static final double BACKWARD_FLING = 3.0D;
    private static final double UPWARD_FLING = 1.2D;

    @Override
    public void tick(IGolemAPI golem) {
        Level level = golem.level();
        if (!level.isClientSide()) {
            return;
        }
        LivingEntity body = golem.asEntity();
        if (!body.onGround() || body.isInWater()) {
            return;
        }
        double movedX = body.getX() - body.xo;
        double movedZ = body.getZ() - body.zo;
        if (movedX * movedX + movedZ * movedZ <= BRISK_SPEED_SQR) {
            return;
        }
        AABB box = body.getBoundingBox();
        BlockPos under = BlockPos.containing(body.getX(), box.minY - GROUND_PROBE, body.getZ());
        BlockState ground = level.getBlockState(under);
        if (ground.isAir() || ground.getRenderShape() == RenderShape.INVISIBLE) {
            return;
        }
        RandomSource random = level.getRandom();
        double x = body.getX() + (random.nextDouble() - CENTER) * box.getXsize();
        double z = body.getZ() + (random.nextDouble() - CENTER) * box.getZsize();
        BlockParticleOption grit = new BlockParticleOption(ParticleTypes.BLOCK, ground, under);
        level.addParticle(grit, x, box.minY + BASE_LIFT, z, -movedX * BACKWARD_FLING, UPWARD_FLING, -movedZ * BACKWARD_FLING);
    }
}
