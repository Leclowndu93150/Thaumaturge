package com.leclowndu93150.thaumaturge.content.essentia.bellows;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.IBellowsPower;
import com.leclowndu93150.thaumaturge.mixin.world.level.block.entity.AbstractFurnaceBlockEntityAccessor;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityBellows extends AbstractSyncedBlockEntity implements IBellowsPower {
    private static final float PUFF_VOLUME = 0.02F;
    private static final float PUFF_BASE_PITCH = 0.5F;
    private static final float PUFF_PITCH_SPREAD = 0.08F;
    private static final int BOOST_INTERVAL = 2;
    private static final int BOOST_STEP = 1;
    private static final int BOOST_MARGIN = 5;
    private long animationOffset;
    private long pausedAt = -1L;
    private boolean animationClockInitialized;

    public BlockEntityBellows(BlockPos pos, BlockState state) {
        super(TTBlockEntities.BELLOWS.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityBellows bellows) {
        long gameTime = level.getGameTime();
        boolean enabled = state.getValue(BlockBellows.ENABLED);
        bellows.updateAnimationClock(gameTime, enabled);
        if (!enabled) {
            return;
        }
        if (BellowsStroke.startsSqueeze(bellows.animationTime(gameTime), pos)) {
            RandomSource random = level.getRandom();
            level.playSound(
                    null,
                    pos,
                    SoundEvents.GHAST_SHOOT,
                    SoundSource.BLOCKS,
                    PUFF_VOLUME,
                    PUFF_BASE_PITCH + (random.nextFloat() - random.nextFloat()) * PUFF_PITCH_SPREAD);
        }
        if (gameTime % BOOST_INTERVAL == 0) {
            bellows.boostFurnace(level, pos.relative(state.getValue(BlockBellows.FACING)));
        }
    }

    private void boostFurnace(Level level, BlockPos target) {
        if (!(level.getBlockEntity(target) instanceof AbstractFurnaceBlockEntity furnace)) {
            return;
        }
        AbstractFurnaceBlockEntityAccessor access = (AbstractFurnaceBlockEntityAccessor) furnace;
        int progress = access.thaumaturge$getCookTime();
        if (progress > 0 && progress < access.thaumaturge$getCookTimeTotal() - BOOST_MARGIN) {
            access.thaumaturge$setCookTime(progress + BOOST_STEP);
            furnace.setChanged();
        }
    }

    public float inflation(float partialTick) {
        if (level == null) {
            return BellowsStroke.FULL;
        }
        boolean enabled = bellowsEnabled();
        return BellowsStroke.inflation(animationTime(level.getGameTime()), enabled ? partialTick : 0.0F, worldPosition);
    }

    private void updateAnimationClock(long gameTime, boolean enabled) {
        if (!animationClockInitialized) {
            animationClockInitialized = true;
            if (!enabled) {
                pausedAt = gameTime;
                animationOffset = gameTime + Mth.getSeed(worldPosition);
                setChangedAndSync();
            } else {
                setChanged();
            }
            return;
        }
        if (!enabled && pausedAt < 0L) {
            pausedAt = gameTime;
            setChangedAndSync();
        } else if (enabled && pausedAt >= 0L) {
            animationOffset += gameTime - pausedAt;
            pausedAt = -1L;
            setChangedAndSync();
        }
    }

    private long animationTime(long gameTime) {
        return (pausedAt >= 0L ? pausedAt : gameTime) - animationOffset;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("AnimationOffset", animationOffset);
        tag.putBoolean("AnimationClockInitialized", animationClockInitialized);
        if (pausedAt >= 0L) {
            tag.putLong("AnimationPausedAt", pausedAt);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        animationOffset = tag.getLong("AnimationOffset");
        pausedAt = tag.contains("AnimationPausedAt") ? tag.getLong("AnimationPausedAt") : -1L;
        animationClockInitialized = tag.getBoolean("AnimationClockInitialized");
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
