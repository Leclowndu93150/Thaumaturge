package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public final class BlockFluxGoo extends LiquidBlock {
    public static final MapCodec<LiquidBlock> CODEC = simpleCodec(p -> (LiquidBlock) new BlockFluxGoo(FluxGooRefs.sourceFluid(), p));

    private static final int REPLACEABLE_AMOUNT_THRESHOLD = 5;
    private static final int AMBIENT_FUME_DENOMINATOR = 44;
    private static final int FUME_GRID = 64;
    private static final int FUME_PARTICLE_INDEX = 64;
    private static final int FUME_FINAL_FRAME_A = 65;
    private static final int FUME_FINAL_FRAME_B = 66;
    private static final float FUME_R = 1.0F;
    private static final float FUME_G = 0.0F;
    private static final float FUME_B = 0.5F;
    private static final float FUME_ALPHA = 0.25F;

    public BlockFluxGoo(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @Override
    public MapCodec<LiquidBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        FluidState fluidState = state.getFluidState();
        return fluidState.getAmount() < REPLACEABLE_AMOUNT_THRESHOLD;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int meta = state.getFluidState().getAmount() - 1;
        if (random.nextInt(AMBIENT_FUME_DENOMINATOR) <= meta) {
            double x = pos.getX() + random.nextFloat();
            double y = pos.getY() + 0.125F * meta;
            double z = pos.getZ() + random.nextFloat();
            float scale = 0.2F + random.nextFloat() * 0.3F;
            int maxAge = 2 + random.nextInt(3);
            BubbleParticleOptions data = new BubbleParticleOptions(ARGB.colorFromFloat(1.0F, FUME_R, FUME_G, FUME_B), FUME_ALPHA, scale, maxAge, -0.01F, false);
            level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public ItemStack pickupBlock(@Nullable LivingEntity user, LevelAccessor level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }
}
