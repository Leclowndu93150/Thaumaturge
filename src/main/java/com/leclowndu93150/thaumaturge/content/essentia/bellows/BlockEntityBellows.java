package com.leclowndu93150.thaumaturge.content.essentia.bellows;

import com.leclowndu93150.thaumaturge.content.essentia.IBellowsPower;
import com.leclowndu93150.thaumaturge.mixin.world.level.block.entity.AbstractFurnaceBlockEntityAccessor;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityBellows extends BlockEntity implements IBellowsPower {
    private static final int STOKE_INTERVAL = 2;
    private static final int STOKE_FINISH_MARGIN = 5;
    private static final float PUFF_VOLUME = 0.02F;
    private static final float PUFF_PITCH = 0.5F;
    private static final float PUFF_PITCH_SPREAD = 0.08F;

    public BlockEntityBellows(BlockPos pos, BlockState state) {
        super(TCBlockEntities.BELLOWS.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityBellows bellows) {
        if (!state.getValue(BlockBellows.ENABLED)) {
            return;
        }
        long time = level.getGameTime();
        if (BellowsStroke.startsSqueeze(time, pos)) {
            RandomSource random = level.getRandom();
            level.playSound(null, pos, SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, PUFF_VOLUME, PUFF_PITCH + random.triangle(0.0F, PUFF_PITCH_SPREAD));
        }
        if (time % STOKE_INTERVAL == 0 && level.getBlockEntity(pos.relative(state.getValue(BlockBellows.FACING))) instanceof AbstractFurnaceBlockEntity furnace) {
            stoke(furnace);
        }
    }

    private static void stoke(AbstractFurnaceBlockEntity furnace) {
        AbstractFurnaceBlockEntityAccessor cooking = (AbstractFurnaceBlockEntityAccessor) furnace;
        int progress = cooking.thaumaturge$getCookTime();
        if (progress > 0 && progress < cooking.thaumaturge$getCookTimeTotal() - STOKE_FINISH_MARGIN) {
            cooking.thaumaturge$setCookTime(progress + 1);
            furnace.setChanged();
        }
    }

    public float inflation(float partialTick) {
        if (level == null || !bellowsEnabled()) {
            return BellowsStroke.FULL;
        }
        return BellowsStroke.inflation(level.getGameTime(), partialTick, worldPosition);
    }

    @Override
    public Direction bellowsFacing() {
        return getBlockState().getValue(BlockBellows.FACING);
    }

    @Override
    public boolean bellowsEnabled() {
        return getBlockState().getValue(BlockBellows.ENABLED);
    }
}
