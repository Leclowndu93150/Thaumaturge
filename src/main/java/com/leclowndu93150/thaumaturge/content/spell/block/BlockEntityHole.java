package com.leclowndu93150.thaumaturge.content.spell.block;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityHole extends AbstractSyncedBlockEntity {
    private static final int DEFAULT_TICKS = 120;
    private static final int NO_HEADING = -1;
    private static final int MIN_SPREAD_DEPTH = 1;
    private static final int RING_REACH = 1;
    private static final int RING_DEPTH = 1;
    private static final int SAVE_INTERVAL = 20;
    private static final int SPARKLE_SKIP_ODDS = 3;
    private static final double HALF_BLOCK = 0.5;
    private static final float SPARKLE_RED = 0.45F;
    private static final float SPARKLE_GREEN = 0.78F;
    private static final float SPARKLE_BLUE = 1.0F;
    private static final float SPARKLE_ALPHA = 1.0F;
    private static final int SPARKLE_COLOR = ARGB.colorFromFloat(SPARKLE_ALPHA, SPARKLE_RED, SPARKLE_GREEN, SPARKLE_BLUE);
    private static final float SPARKLE_BASE_SCALE = 0.6F;
    private static final float SPARKLE_SCALE_RANGE = 0.2F;
    private static final int SPARKLE_DELAY = 0;
    private static final float SPARKLE_DECAY = 1.0F;
    private static final float SPARKLE_GRAVITY = 0.0F;
    private static final int SPARKLE_BASE_AGE = 2;
    private static final String REPLACED_KEY = "replaced_state";
    private static final String ELAPSED_KEY = "elapsed_ticks";
    private static final String DURATION_KEY = "duration_ticks";
    private static final String DEPTH_KEY = "remaining_depth";
    private static final String HEADING_KEY = "heading";
    private static final String LEGACY_REPLACED_KEY = "oldblock";
    private static final String LEGACY_ELAPSED_KEY = "countdown";
    private static final String LEGACY_DURATION_KEY = "countdownmax";
    private static final String LEGACY_DEPTH_KEY = "count";
    private static final String LEGACY_HEADING_KEY = "direction";
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction.Axis[] AXES = Direction.Axis.values();

    private BlockState replaced = Blocks.AIR.defaultBlockState();
    private int elapsed;
    private int maxTicks = DEFAULT_TICKS;
    private int depth;
    private @Nullable Direction heading;

    public BlockEntityHole(BlockPos pos, BlockState state) {
        super(TTBlockEntities.HOLE.get(), pos, state);
    }

    public void configure(BlockState replaced, int ticks, int depth, @Nullable Direction heading) {
        this.elapsed = 0;
        this.heading = heading;
        this.depth = depth;
        this.maxTicks = ticks;
        this.replaced = replaced;
        setChangedAndSync();
    }

    public BlockState originalBlockState() {
        return replaced;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityHole hole) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        hole.elapsed++;
        if (hole.depth > MIN_SPREAD_DEPTH && hole.heading != null) {
            hole.spread(server, pos, hole.heading);
        }
        if (hole.elapsed >= hole.maxTicks) {
            server.setBlock(pos, hole.replaced, Block.UPDATE_ALL);
            return;
        }
        if (hole.elapsed % SAVE_INTERVAL == 0) {
            hole.setChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityHole hole) {
        RandomSource random = level.getRandom();
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        for (Direction side : DIRECTIONS) {
            if (!open(level, probe.setWithOffset(pos, side))) {
                continue;
            }
            for (Direction edge : DIRECTIONS) {
                if (edge.getAxis() == side.getAxis() || random.nextInt(SPARKLE_SKIP_ODDS) != 0) {
                    continue;
                }
                boolean beside = opaque(level, probe.setWithOffset(pos, edge));
                if (beside || opaque(level, probe.move(side))) {
                    sparkle(level, pos, side, edge, random);
                }
            }
        }
    }

    private void spread(ServerLevel level, BlockPos pos, Direction face) {
        int remaining = depth;
        depth = MIN_SPREAD_DEPTH;
        setChanged();
        Direction.Axis across = face.getAxis();
        BlockPos.MutableBlockPos ring = new BlockPos.MutableBlockPos();
        for (int a = -RING_REACH; a <= RING_REACH; a++) {
            for (int b = -RING_REACH; b <= RING_REACH; b++) {
                if (a == 0 && b == 0) {
                    continue;
                }
                PortableHoles.open(level, offset(ring, pos, across, a, b), null, RING_DEPTH, maxTicks);
            }
        }
        PortableHoles.open(level, pos.relative(face.getOpposite()), face, remaining - 1, maxTicks);
    }

    private static BlockPos offset(BlockPos.MutableBlockPos target, BlockPos centre, Direction.Axis across, int a, int b) {
        return switch (across) {
            case X -> target.setWithOffset(centre, 0, a, b);
            case Y -> target.setWithOffset(centre, a, 0, b);
            case Z -> target.setWithOffset(centre, a, b, 0);
        };
    }

    private static void sparkle(Level level, BlockPos pos, Direction side, Direction edge, RandomSource random) {
        Direction.Axis line = lineAxis(side.getAxis(), edge.getAxis());
        double along = random.nextDouble() - HALF_BLOCK;
        double x = pos.getX() + HALF_BLOCK + HALF_BLOCK * (side.getStepX() + edge.getStepX()) + (line == Direction.Axis.X ? along : 0.0);
        double y = pos.getY() + HALF_BLOCK + HALF_BLOCK * (side.getStepY() + edge.getStepY()) + (line == Direction.Axis.Y ? along : 0.0);
        double z = pos.getZ() + HALF_BLOCK + HALF_BLOCK * (side.getStepZ() + edge.getStepZ()) + (line == Direction.Axis.Z ? along : 0.0);
        float scale = SPARKLE_BASE_SCALE + random.nextFloat() * SPARKLE_SCALE_RANGE;
        SparkleParticleOptions options = new SparkleParticleOptions(SPARKLE_COLOR, scale, SPARKLE_DELAY, SPARKLE_DECAY, SPARKLE_GRAVITY, SPARKLE_BASE_AGE, true);
        level.addParticle(options, x, y, z, 0.0, 0.0, 0.0);
    }

    private static Direction.Axis lineAxis(Direction.Axis first, Direction.Axis second) {
        for (Direction.Axis axis : AXES) {
            if (axis != first && axis != second) {
                return axis;
            }
        }
        return first;
    }

    private static boolean open(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.is(TTBlocks.HOLE.get()) && !state.isSolidRender();
    }

    private static boolean opaque(Level level, BlockPos pos) {
        return level.getBlockState(pos).isSolidRender();
    }

    private static int headingOrdinal(@Nullable Direction direction) {
        return direction != null ? direction.ordinal() : NO_HEADING;
    }

    private static int readInt(ValueInput input, String key, String legacyKey, int fallback) {
        return input.getInt(key).or(() -> input.getInt(legacyKey)).orElse(fallback);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(HEADING_KEY, headingOrdinal(heading));
        output.putInt(DEPTH_KEY, depth);
        output.putInt(DURATION_KEY, maxTicks);
        output.putInt(ELAPSED_KEY, elapsed);
        output.store(REPLACED_KEY, BlockState.CODEC, replaced);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        replaced = input.read(REPLACED_KEY, BlockState.CODEC).or(() -> input.read(LEGACY_REPLACED_KEY, BlockState.CODEC)).orElse(Blocks.AIR.defaultBlockState());
        elapsed = readInt(input, ELAPSED_KEY, LEGACY_ELAPSED_KEY, 0);
        maxTicks = readInt(input, DURATION_KEY, LEGACY_DURATION_KEY, DEFAULT_TICKS);
        depth = readInt(input, DEPTH_KEY, LEGACY_DEPTH_KEY, 0);
        int ordinal = readInt(input, HEADING_KEY, LEGACY_HEADING_KEY, NO_HEADING);
        heading = ordinal >= 0 && ordinal < DIRECTIONS.length ? DIRECTIONS[ordinal] : null;
    }
}
