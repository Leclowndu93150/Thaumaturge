package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class BlockEssentiaTransport extends BaseEntityBlock {
    private static final int FLUX_VENT_COUNT = 5;
    private static final float FLUX_VENT_SCALE = 1.0F;

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final double CORE_MIN = 5.0 / 16.0;
    private static final double CORE_MAX = 11.0 / 16.0;
    private static final VoxelShape CORE = Block.box(CORE_MIN * 16.0, CORE_MIN * 16.0, CORE_MIN * 16.0, CORE_MAX * 16.0, CORE_MAX * 16.0, CORE_MAX * 16.0);
    private static final VoxelShape STUB_DOWN = Block.box(CORE_MIN * 16.0, 0.0, CORE_MIN * 16.0, CORE_MAX * 16.0, CORE_MAX * 16.0, CORE_MAX * 16.0);
    private static final VoxelShape STUB_UP = Block.box(CORE_MIN * 16.0, CORE_MIN * 16.0, CORE_MIN * 16.0, CORE_MAX * 16.0, 16.0, CORE_MAX * 16.0);
    private static final VoxelShape STUB_NORTH = Block.box(CORE_MIN * 16.0, CORE_MIN * 16.0, 0.0, CORE_MAX * 16.0, CORE_MAX * 16.0, CORE_MAX * 16.0);
    private static final VoxelShape STUB_SOUTH = Block.box(CORE_MIN * 16.0, CORE_MIN * 16.0, CORE_MIN * 16.0, CORE_MAX * 16.0, CORE_MAX * 16.0, 16.0);
    private static final VoxelShape STUB_WEST = Block.box(0.0, CORE_MIN * 16.0, CORE_MIN * 16.0, CORE_MAX * 16.0, CORE_MAX * 16.0, CORE_MAX * 16.0);
    private static final VoxelShape STUB_EAST = Block.box(CORE_MIN * 16.0, CORE_MIN * 16.0, CORE_MIN * 16.0, 16.0, CORE_MAX * 16.0, CORE_MAX * 16.0);

    protected BlockEssentiaTransport(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false).setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        if (state.getValue(DOWN))
            shape = Shapes.or(shape, STUB_DOWN);
        if (state.getValue(UP))
            shape = Shapes.or(shape, STUB_UP);
        if (state.getValue(NORTH))
            shape = Shapes.or(shape, STUB_NORTH);
        if (state.getValue(SOUTH))
            shape = Shapes.or(shape, STUB_SOUTH);
        if (state.getValue(WEST))
            shape = Shapes.or(shape, STUB_WEST);
        if (state.getValue(EAST))
            shape = Shapes.or(shape, STUB_EAST);
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    public static BooleanProperty propertyFor(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    public static BlockState recomputeConnections(BlockState state, LevelReader level, BlockPos pos, Direction... ignored) {
        BlockState next = state;
        if (!(level instanceof Level lvl)) {
            return next;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            if (Arrays.stream(ignored).anyMatch(d -> d == direction))
                continue;
            cursor.setWithOffset(pos, direction);
            boolean connect = canConnectTo(lvl, cursor.immutable(), direction.getOpposite());
            next = next.setValue(propertyFor(direction), connect);
        }
        return next;
    }

    public static boolean canConnectTo(Level level, BlockPos neighbourPos, Direction faceFromNeighbour) {
        IEssentiaTransport remote = level.getCapability(EssentiaCapabilities.TRANSPORT, neighbourPos, faceFromNeighbour);
        return remote != null && remote.isConnectable(faceFromNeighbour);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (level instanceof Level lvl) {
            boolean connect = canConnectTo(lvl, neighbourPos, directionToNeighbour.getOpposite());
            return state.setValue(propertyFor(directionToNeighbour), connect);
        }
        return state;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)
            return null;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return recomputeConnections(state, level, pos);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return false;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        spillEssentiaOnBreak(level, pos);
        return super.playerWillDestroy(level, pos, state, player);
    }

    protected static void spillEssentiaOnBreak(Level level, BlockPos pos) {
        BlockEntity te = level.getBlockEntity(pos);
        if (!(te instanceof IEssentiaTransport transport))
            return;
        int amount = transport.getEssentiaAmount(Direction.UP);
        if (amount <= 0)
            return;
        if (!(level instanceof ServerLevel server))
            return;
        AuraHelper.addFlux(server, pos, amount);
        RandomSource rand = server.getRandom();
        server.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.1F, 1.0F + rand.nextFloat() * 0.1F);
        for (int i = 0; i < FLUX_VENT_COUNT; i++) {
            double ox = pos.getX() + 0.33 + rand.nextFloat() * 0.33;
            double oy = pos.getY() + 0.33 + rand.nextFloat() * 0.33;
            double oz = pos.getZ() + 0.33 + rand.nextFloat() * 0.33;
            Effects.vent2(server, new Vec3(ox, oy, oz)).motion(0.0, 0.1 + rand.nextFloat() * 0.2, 0.0).color(0x800080).scale(FLUX_VENT_SCALE).withFlame().send();
        }
    }

    public static @Nullable BlockState refreshConnections(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockEssentiaTransport tube))
            return null;
        if (level instanceof LevelReader reader) {
            BlockState next = tube.recomputeConnections(state, reader, pos);
            if (!next.equals(state)) {
                level.setBlock(pos, next, Block.UPDATE_ALL);
            }
            return next;
        }
        return state;
    }
}
