package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.infusion.IInfusionStabiliser;
import com.leclowndu93150.thaumaturge.content.infusion.BlockPedestal;
import com.mojang.serialization.MapCodec;
import java.util.ArrayDeque;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockInlay extends Block implements IInfusionStabiliser {
    private static final int MAX_CHARGE = 15;
    private static final int NO_CHARGE = -1;
    private static final int NETWORK_ITERATION_LIMIT = 4096;
    private static final float STABILISATION = 0.025F;
    private static final int DISPLAY_CHANCE_BASE = 20;
    private static final double DISPLAY_SPREAD = 0.08;
    private static final double DISPLAY_HEIGHT = 0.05;
    private static final double CENTER = 0.5;
    private static final Direction[] HORIZONTALS = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
    private static final VoxelShape SHAPE = box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    public static final MapCodec<BlockInlay> CODEC = simpleCodec(BlockInlay::new);
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, MAX_CHARGE);
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final int MITIGATOR_MIN_ENERGY = 5;

    public BlockInlay(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CHARGE, 0).setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
    }

    @Override
    protected MapCodec<BlockInlay> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos floor = pos.relative(Direction.DOWN);
        return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos placed = context.getClickedPos();
        return withConnections(defaultBlockState(), context.getLevel(), placed);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return direction.getAxis().isHorizontal() ? withConnections(state, level, pos) : state;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide() && !oldState.is(this)) {
            updateNetwork(level, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        for (Direction direction : HORIZONTALS) {
            BlockPos neighbor = pos.relative(direction);
            if (level.hasChunkAt(neighbor) && level.getBlockState(neighbor).getBlock() instanceof BlockInlay) {
                updateNetwork(level, neighbor);
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) {
            return;
        }
        if (!state.canSurvive(level, pos)) {
            dropResources(state, level, pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        updateNetwork(level, pos);
    }

    public static int chargeAt(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof BlockInlay) {
            return state.getValue(CHARGE);
        }
        return block instanceof BlockPedestal ? state.getValue(BlockPedestal.CHARGE) : NO_CHARGE;
    }

    public static int sourceStrengthAt(Level level, BlockPos pos) {
        if (!(level.getBlockState(pos).getBlock() instanceof BlockStabilizer)) {
            return 0;
        }
        return level.getBlockEntity(pos) instanceof BlockEntityStabilizer stabilizer ? Math.max(0, stabilizer.charge()) : 0;
    }

    public static void updateNetwork(Level level, BlockPos origin) {
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        pending.add(origin);
        int iterations = 0;
        while (!pending.isEmpty() && iterations < NETWORK_ITERATION_LIMIT) {
            iterations++;
            BlockPos pos = pending.poll();
            if (!level.hasChunkAt(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            IntegerProperty property = chargeProperty(state);
            if (property == null) {
                continue;
            }
            int target = targetCharge(level, pos);
            if (target == state.getValue(property)) {
                continue;
            }
            level.setBlock(pos, state.setValue(property, target), Block.UPDATE_CLIENTS);
            for (Direction direction : HORIZONTALS) {
                BlockPos neighbor = pos.relative(direction);
                if (level.hasChunkAt(neighbor) && chargeAt(level, neighbor) != NO_CHARGE) {
                    pending.add(neighbor);
                }
            }
        }
    }

    @Override
    public float getStabilizationAmount(Level level, BlockPos pos) {
        return STABILISATION;
    }

    @Override
    public boolean canStabiliseInfusion(Level level, BlockPos pos) {
        return STABILISATION > 0.0F;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int charge = state.getValue(CHARGE);
        boolean sparks = charge > 0 && random.nextInt(DISPLAY_CHANCE_BASE - charge) == 0;
        if (sparks) {
            double x = jitter(pos.getX(), random);
            double z = jitter(pos.getZ(), random);
            level.addParticle(DustParticleOptions.REDSTONE, x, pos.getY() + DISPLAY_HEIGHT, z, 0.0, 0.0, 0.0);
        }
    }

    private static double jitter(int coordinate, RandomSource random) {
        return coordinate + CENTER + random.nextGaussian() * DISPLAY_SPREAD;
    }

    private static @Nullable IntegerProperty chargeProperty(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlockInlay) {
            return CHARGE;
        }
        return block instanceof BlockPedestal ? BlockPedestal.CHARGE : null;
    }

    private static int targetCharge(Level level, BlockPos pos) {
        int target = 0;
        for (Direction direction : HORIZONTALS) {
            target = Math.max(target, drivenBy(level, pos.relative(direction)));
        }
        return Math.min(target, MAX_CHARGE);
    }

    private static int drivenBy(Level level, BlockPos neighbor) {
        if (!level.hasChunkAt(neighbor)) {
            return 0;
        }
        return Math.max(sourceStrengthAt(level, neighbor), chargeAt(level, neighbor) - 1);
    }

    private static BlockState withConnections(BlockState state, LevelReader level, BlockPos pos) {
        BlockState result = state;
        for (Direction direction : HORIZONTALS) {
            result = result.setValue(connection(direction), connects(level.getBlockState(pos.relative(direction))));
        }
        return result;
    }

    private static BooleanProperty connection(Direction direction) {
        return switch (direction) {
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    private static boolean connects(BlockState neighbor) {
        Block block = neighbor.getBlock();
        return block instanceof BlockInlay || block instanceof BlockPedestal || block instanceof BlockStabilizer;
    }
}
