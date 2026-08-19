package com.leclowndu93150.thaumaturge.content.alchemy;

import com.leclowndu93150.thaumaturge.content.particle.SlimyBubbleParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class BlockLiquidDeath extends LiquidBlock {
    private static final int BUBBLE_CHANCE = 20;
    private static final int POP_CHANCE = 50;
    private static final float SURFACE_BASE = 0.1F;
    private static final float SURFACE_PER_LEVEL = 0.225F;
    private static final int LEVELS_PER_TC_LEVEL = 2;
    private static final float BUBBLE_ALPHA = 0.8F;
    private static final float BUBBLE_SCALE_BASE = 0.075F;
    private static final int BUBBLE_AGE_BASE = 15;
    private static final int BUBBLE_AGE_SPREAD = 5;
    private static final float RED_BASE = 0.3F;
    private static final float RED_SPREAD = 0.1F;
    private static final float BLUE_BASE = 0.4F;
    private static final float BLUE_SPREAD = 0.1F;

    public BlockLiquidDeath(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(BUBBLE_CHANCE) == 0) {
            int amount = level.getFluidState(pos).getAmount();
            int color = ARGB.colorFromFloat(1.0F, RED_BASE - random.nextFloat() * RED_SPREAD, 0.0F, BLUE_BASE + random.nextFloat() * BLUE_SPREAD);
            SlimyBubbleParticleOptions bubble = new SlimyBubbleParticleOptions(color, BUBBLE_ALPHA, BUBBLE_SCALE_BASE + random.nextFloat() * BUBBLE_SCALE_BASE,
                    BUBBLE_AGE_BASE + random.nextInt(BUBBLE_AGE_SPREAD));
            level.addParticle(bubble, pos.getX() + random.nextFloat(), pos.getY() + SURFACE_BASE + SURFACE_PER_LEVEL * (amount / (float) LEVELS_PER_TC_LEVEL), pos.getZ() + random.nextFloat(), 0.0,
                    0.0, 0.0);
        }
        if (random.nextInt(POP_CHANCE) == 0) {
            level.playLocalSound(pos.getX() + random.nextFloat(), pos.getY() + 0.5, pos.getZ() + random.nextFloat(), SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + random.nextFloat() * 0.1F,
                    0.9F + random.nextFloat() * 0.15F, false);
        }
    }
}
