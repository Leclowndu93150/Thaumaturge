package com.leclowndu93150.thaumaturge.content.alchemy;

import com.leclowndu93150.thaumaturge.api.damagesource.TCDamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class LiquidDeathFluid extends BaseFlowingFluid {
    private static final float MAX_AMOUNT = 8.0F;
    private static final float BASE_DAMAGE = 1.0F;
    private static final int LEVELS_PER_DAMAGE_STEP = 2;

    protected LiquidDeathFluid(Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier) {
        FluidState fs = level.getFluidState(pos);
        int amount = fs.getAmount();
        Vec3 motion = entity.getDeltaMovement();
        double damp = 1.0 - amount / MAX_AMOUNT / 2.0;
        entity.setDeltaMovement(motion.x * damp, motion.y, motion.z * damp);
        if (!level.isClientSide() && entity instanceof LivingEntity) {
            entity.hurt(TCDamageSources.dissolve(level), damageFor(amount));
        }
    }

    private static float damageFor(int amount) {
        return BASE_DAMAGE + Mth.ceil(amount / (float) LEVELS_PER_DAMAGE_STEP);
    }

    public static final class Source extends LiquidDeathFluid {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return Mth.floor(MAX_AMOUNT);
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static final class Flowing extends LiquidDeathFluid {
        public Flowing(Properties properties) {
            super(properties);
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}
