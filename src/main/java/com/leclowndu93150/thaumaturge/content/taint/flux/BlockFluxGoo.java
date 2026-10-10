package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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

public final class BlockFluxGoo extends LiquidBlock implements PhysicalFluxBlock {
    public static final MapCodec<LiquidBlock> CODEC = simpleCodec(properties -> new BlockFluxGoo(FluxGooRefs.sourceFluid(), properties));

    private static final int REPLACEABLE_AMOUNT = 2;
    private static final float AURA_FLOOR_PER_QUANTUM = 0.5F;
    private static final float TAINT_WEIGHT_PER_QUANTUM = 1.0F;
    private static final int OUTBREAK_COST = 0;
    private static final int BUBBLE_ROLL_RANGE = 48;
    private static final int BUBBLE_COLOR = ARGB.color(0xD8, 0x3C, 0xE6);
    private static final float BUBBLE_ALPHA = 0.45F;
    private static final float BUBBLE_MIN_SCALE = 0.25F;
    private static final float BUBBLE_SCALE_SPREAD = 0.3F;
    private static final int BUBBLE_MIN_AGE = 18;
    private static final int BUBBLE_AGE_SPREAD = 14;
    private static final float BUBBLE_BUOYANCY = -0.008F;

    public BlockFluxGoo(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @Override
    public MapCodec<LiquidBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return fluxAmount(state) <= REPLACEABLE_AMOUNT;
    }

    @Override
    public int fluxAmount(BlockState state) {
        return Math.clamp(state.getFluidState().getAmount(), 1, PhysicalFlux.MAX_QUANTA);
    }

    @Override
    public BlockState withFluxAmount(int amount) {
        return FluxGooFluid.gooBlockState(amount);
    }

    @Override
    public void scheduleFluxTick(ServerLevel level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        if (!fluidState.isEmpty()) {
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
    }

    @Override
    public float auraFloorPerQuantum() {
        return AURA_FLOOR_PER_QUANTUM;
    }

    @Override
    public float taintWeightPerQuantum() {
        return TAINT_WEIGHT_PER_QUANTUM;
    }

    @Override
    public int outbreakCost() {
        return OUTBREAK_COST;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel) {
            PhysicalFluxAuraFloor.observe(serverLevel, pos);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        FluidState fluid = state.getFluidState();
        if (fluid.isEmpty() || random.nextInt(BUBBLE_ROLL_RANGE) >= fluxAmount(state)) {
            return;
        }
        float scale = BUBBLE_MIN_SCALE + random.nextFloat() * BUBBLE_SCALE_SPREAD;
        int age = BUBBLE_MIN_AGE + random.nextInt(BUBBLE_AGE_SPREAD);
        BubbleParticleOptions bubble = new BubbleParticleOptions(BUBBLE_COLOR, BUBBLE_ALPHA, scale, age, BUBBLE_BUOYANCY, false);
        level.addParticle(bubble, pos.getX() + random.nextDouble(), pos.getY() + fluid.getOwnHeight(), pos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
    }

    @Override
    public ItemStack pickupBlock(@Nullable LivingEntity user, LevelAccessor level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }
}
