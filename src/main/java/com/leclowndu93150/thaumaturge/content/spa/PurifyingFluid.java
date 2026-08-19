package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class PurifyingFluid extends BaseFlowingFluid {
    private static final float MAX_AMOUNT = 8.0F;
    private static final int WARD_BUDGET_TICKS = 200000;
    private static final int WARD_MAX_TICKS = 32000;

    protected PurifyingFluid(Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier) {
        FluidState fs = level.getFluidState(pos);
        float quanta = fs.getAmount() / MAX_AMOUNT;
        Vec3 motion = entity.getDeltaMovement();
        double damp = 1.0 - quanta / 2.0;
        entity.setDeltaMovement(motion.x * damp, motion.y, motion.z * damp);
        if (!level.isClientSide() && fs.isSource() && entity instanceof ServerPlayer player && !player.hasEffect(TCMobEffects.WARP_WARD)) {
            int permanent = WarpHelper.getWarp(player).get(WarpType.PERMANENT);
            int div = permanent > 0 ? Math.max(1, (int) Math.sqrt(permanent)) : 1;
            player.addEffect(new MobEffectInstance(TCMobEffects.WARP_WARD, Math.min(WARD_MAX_TICKS, WARD_BUDGET_TICKS / div), 0, true, true));
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    public static final class Source extends PurifyingFluid {
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

    public static final class Flowing extends PurifyingFluid {
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
