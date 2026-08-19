package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.infusion.IInfusionStabiliser;
import com.leclowndu93150.thaumaturge.content.infusion.BlockPedestal;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.Set;
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
import net.minecraft.world.level.block.SupportType;
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
    public static final MapCodec<BlockInlay> CODEC = simpleCodec(BlockInlay::new);
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 15);
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final float STABILIZATION = 0.025F;
    public static final int MITIGATOR_MIN_ENERGY = 5;

    public BlockInlay(Properties properties) {
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
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP, SupportType.FULL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connectionState(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    private static boolean connectsTo(BlockState state) {
        return state.is(TCBlocks.INLAY.get()) || state.getBlock() instanceof BlockPedestal;
    }

    private static boolean isSourceBlock(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(TCBlocks.STABILIZER.get());
    }

    private static BlockState connectionState(BlockState state, LevelReader level, BlockPos pos) {
        return state.setValue(NORTH, attaches(level, pos, Direction.NORTH)).setValue(EAST, attaches(level, pos, Direction.EAST)).setValue(SOUTH, attaches(level, pos, Direction.SOUTH)).setValue(WEST,
                attaches(level, pos, Direction.WEST));
    }

    private static boolean attaches(LevelReader level, BlockPos pos, Direction direction) {
        BlockPos neighbour = pos.relative(direction);
        BlockState state = level.getBlockState(neighbour);
        return connectsTo(state) || isSourceBlock(level, neighbour);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (direction.getAxis().isHorizontal()) {
            return connectionState(state, level, pos);
        }
        return state;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide() && !oldState.is(this)) {
            updateNetwork(level, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = pos.relative(direction);
            if (level.getBlockState(neighbour).is(this)) {
                updateNetwork(level, neighbour);
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) {
            return;
        }
        if (!canSurvive(state, level, pos)) {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
            return;
        }
        updateNetwork(level, pos);
    }

    public static int chargeAt(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(CHARGE)) {
            return state.getValue(CHARGE);
        }
        if (state.hasProperty(BlockPedestal.CHARGE)) {
            return state.getValue(BlockPedestal.CHARGE);
        }
        return -1;
    }

    public static int sourceStrengthAt(Level level, BlockPos pos) {
        if (isSourceBlock(level, pos) && level.getBlockEntity(pos) instanceof BlockEntityStabilizer stabilizer) {
            return stabilizer.getEnergy();
        }
        return 0;
    }

    public static void updateNetwork(Level level, BlockPos origin) {
        Set<BlockPos> pending = new HashSet<>();
        pending.add(origin.immutable());
        int guard = 0;
        while (!pending.isEmpty() && guard++ < 4096) {
            BlockPos pos = pending.iterator().next();
            pending.remove(pos);
            BlockState state = level.getBlockState(pos);
            int current;
            IntegerProperty property;
            if (state.hasProperty(CHARGE)) {
                property = CHARGE;
                current = state.getValue(CHARGE);
            } else if (state.hasProperty(BlockPedestal.CHARGE)) {
                property = BlockPedestal.CHARGE;
                current = state.getValue(BlockPedestal.CHARGE);
            } else {
                continue;
            }
            int best = 0;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighbourPos = pos.relative(direction);
                int source = sourceStrengthAt(level, neighbourPos);
                if (source > best) {
                    best = source;
                }
                int neighbour = chargeAt(level, neighbourPos);
                if (neighbour - 1 > best) {
                    best = neighbour - 1;
                }
            }
            if (best != current) {
                level.setBlock(pos, state.setValue(property, best), 2);
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos neighbourPos = pos.relative(direction);
                    if (chargeAt(level, neighbourPos) >= 0) {
                        pending.add(neighbourPos.immutable());
                    }
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int charge = state.getValue(CHARGE);
        if (charge > 0 && random.nextInt(20 - charge) == 0) {
            level.addParticle(DustParticleOptions.REDSTONE, pos.getX() + 0.5 + random.nextGaussian() * 0.08, pos.getY() + 0.05, pos.getZ() + 0.5 + random.nextGaussian() * 0.08, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public boolean canStabiliseInfusion(Level level, BlockPos pos) {
        return true;
    }

    @Override
    public float getStabilizationAmount(Level level, BlockPos pos) {
        return STABILIZATION;
    }
}
