package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class BlockPurifyingFluid extends LiquidBlock {
    private static final int MOTE_CHANCE = 10;
    private static final int POP_CHANCE = 50;
    private static final int MOTE_FRAME = 64;
    private static final float LEVEL_HEIGHT = 0.125F;
    private static final int MAX_AMOUNT = 8;

    public BlockPurifyingFluid(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(MOTE_CHANCE) == 0) {
            int amount = level.getFluidState(pos).getAmount();
            BubbleParticleOptions data = new BubbleParticleOptions(0xFFFFFF, 0.25F, random.nextFloat() * 0.3F + 0.3F, 10 + random.nextInt(10), -0.01F, false);
            level.addParticle(data, pos.getX() + random.nextFloat(), pos.getY() + LEVEL_HEIGHT * amount, pos.getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
        }
        if (random.nextInt(POP_CHANCE) == 0) {
            level.playLocalSound(pos.getX() + random.nextFloat(), pos.getY() + 0.5, pos.getZ() + random.nextFloat(), SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + random.nextFloat() * 0.1F,
                    0.9F + random.nextFloat() * 0.15F, false);
        }
    }
}
