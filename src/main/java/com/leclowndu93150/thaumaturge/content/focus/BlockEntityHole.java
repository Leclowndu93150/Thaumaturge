package com.leclowndu93150.thaumaturge.content.focus;

import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectRift;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityHole extends BlockEntity {
    private static final int DEFAULT_COUNTDOWN_MAX = 120;
    private static final int DIRTY_INTERVAL_TICKS = 20;
    private static final int SPARKLE_ATTEMPTS_PER_TICK = 2;
    private static final int RING_CELLS = 9;
    private static final int RING_SIZE = 3;
    private static final float SPARKLE_RED = 0.25F;
    private static final float SPARKLE_GREEN = 0.25F;
    private static final float SPARKLE_BLUE = 1.0F;
    private static final float SPARKLE_ALPHA = 0.9F;

    BlockState oldblock = Blocks.AIR.defaultBlockState();
    int countdown = 0;
    int countdownmax = DEFAULT_COUNTDOWN_MAX;
    int count = 0;

    @Nullable
    Direction direction = null;

    public BlockEntityHole(BlockPos pos, BlockState state) {
        super(TCBlockEntities.HOLE.get(), pos, state);
    }

    public void configure(BlockState oldblock, int countdownmax, int count, @Nullable Direction direction) {
        this.oldblock = oldblock;
        this.countdownmax = countdownmax;
        this.count = count;
        this.direction = direction;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityHole hole) {
        if (hole.countdown == 0 && hole.count > 1 && hole.direction != null) {
            switch (hole.direction.getAxis()) {
                case Y -> {
                    for (int a = 0; a < RING_CELLS; a++) {
                        if (a / RING_SIZE != 1 || a % RING_SIZE != 1) {
                            FocusEffectRift.createHole(level, pos.offset(-1 + a / RING_SIZE, 0, -1 + a % RING_SIZE), null, 1, hole.countdownmax);
                        }
                    }
                }
                case Z -> {
                    for (int a = 0; a < RING_CELLS; a++) {
                        if (a / RING_SIZE != 1 || a % RING_SIZE != 1) {
                            FocusEffectRift.createHole(level, pos.offset(-1 + a / RING_SIZE, -1 + a % RING_SIZE, 0), null, 1, hole.countdownmax);
                        }
                    }
                }
                case X -> {
                    for (int a = 0; a < RING_CELLS; a++) {
                        if (a / RING_SIZE != 1 || a % RING_SIZE != 1) {
                            FocusEffectRift.createHole(level, pos.offset(0, -1 + a / RING_SIZE, -1 + a % RING_SIZE), null, 1, hole.countdownmax);
                        }
                    }
                }
            }
            if (!FocusEffectRift.createHole(level, pos.relative(hole.direction.getOpposite()), hole.direction, hole.count - 1, hole.countdownmax)) {
                hole.count = 0;
            }
        }
        hole.countdown++;
        if (hole.countdown % DIRTY_INTERVAL_TICKS == 0) {
            hole.setChanged();
        }
        if (hole.countdown >= hole.countdownmax) {
            level.setBlock(pos, hole.oldblock, Block.UPDATE_ALL);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityHole hole) {
        for (int a = 0; a < SPARKLE_ATTEMPTS_PER_TICK; a++) {
            hole.surroundWithSparkles(level, pos);
        }
    }

    private void surroundWithSparkles(Level level, BlockPos pos) {
        for (Direction d1 : Direction.values()) {
            BlockState b1 = level.getBlockState(pos.relative(d1));
            if (b1.is(getBlockState().getBlock()) || b1.isSolidRender()) {
                continue;
            }
            for (Direction d2 : Direction.values()) {
                if (d1.getAxis() == d2.getAxis()) {
                    continue;
                }
                if (!level.getBlockState(pos.relative(d2)).isSolidRender() && !level.getBlockState(pos.relative(d1).relative(d2)).isSolidRender()) {
                    continue;
                }
                RandomSource rand = level.getRandom();
                float sx = 0.5F * d1.getStepX();
                float sy = 0.5F * d1.getStepY();
                float sz = 0.5F * d1.getStepZ();
                if (sx == 0.0F) {
                    sx = 0.5F * d2.getStepX();
                }
                if (sy == 0.0F) {
                    sy = 0.5F * d2.getStepY();
                }
                if (sz == 0.0F) {
                    sz = 0.5F * d2.getStepZ();
                }
                sx = sx == 0.0F ? rand.nextFloat() : sx + 0.5F;
                sy = sy == 0.0F ? rand.nextFloat() : sy + 0.5F;
                sz = sz == 0.0F ? rand.nextFloat() : sz + 0.5F;
                spawnSparkle(level, pos.getX() + sx, pos.getY() + sy, pos.getZ() + sz);
            }
        }
    }

    private static void spawnSparkle(Level level, double x, double y, double z) {
        RandomSource rand = level.getRandom();
        if (rand.nextInt(6) >= 4) {
            return;
        }
        SparkleParticleOptions data = new SparkleParticleOptions(ARGB.colorFromFloat(1.0F, SPARKLE_RED, SPARKLE_GREEN, SPARKLE_BLUE), 0.6F + rand.nextFloat() * 0.2F, 0, 1.0F, 0.0F, 2, false);
        level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("oldblock", BlockState.CODEC, oldblock);
        output.putInt("countdown", countdown);
        output.putInt("countdownmax", countdownmax);
        output.putInt("count", count);
        output.putInt("direction", direction == null ? -1 : direction.ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        oldblock = input.read("oldblock", BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState());
        countdown = input.getIntOr("countdown", 0);
        countdownmax = input.getIntOr("countdownmax", DEFAULT_COUNTDOWN_MAX);
        count = input.getIntOr("count", 0);
        int directionOrdinal = input.getIntOr("direction", -1);
        direction = directionOrdinal >= 0 ? Direction.values()[directionOrdinal] : null;
    }
}
