package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.blocks.ILabelable;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.leclowndu93150.thaumaturge.content.essentia.storage.LabelledVesselActions;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockAlembic extends BaseEntityBlock implements ILabelable {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    private static final MapCodec<BlockAlembic> CODEC = simpleCodec(BlockAlembic::new);
    private static final Direction[] SIDES = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private static final Map<Direction, BooleanProperty> SIDE_PROPERTIES = sideProperties();
    private static final VoxelShape[] SHAPES = buildShapes();
    private static final int COMPARATOR_STEPS = 14;

    public BlockAlembic(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
    }

    private static Map<Direction, BooleanProperty> sideProperties() {
        Map<Direction, BooleanProperty> properties = new EnumMap<>(Direction.class);
        properties.put(Direction.NORTH, NORTH);
        properties.put(Direction.EAST, EAST);
        properties.put(Direction.SOUTH, SOUTH);
        properties.put(Direction.WEST, WEST);
        return properties;
    }

    private static VoxelShape[] buildShapes() {
        VoxelShape body = Shapes.join(Block.box(1, 0, 1, 15, 16, 15), Block.box(5, 15, 5, 11, 16, 11), BooleanOp.ONLY_FIRST);
        VoxelShape window = Block.box(3, 2, 1, 13, 14, 2);
        VoxelShape spigot = Shapes.or(Block.box(5, 5, 0, 11, 11, 1), Block.box(6, 6, 1, 10, 10, 2));
        VoxelShape[] spigots = new VoxelShape[SIDES.length];
        for (int i = 0; i < SIDES.length; i++) {
            body = Shapes.join(body, DeviceShapes.rotate(window, 0, i), BooleanOp.ONLY_FIRST);
            spigots[i] = DeviceShapes.rotate(spigot, 0, i);
        }
        VoxelShape[] shapes = new VoxelShape[1 << SIDES.length];
        for (int mask = 0; mask < shapes.length; mask++) {
            VoxelShape shape = body;
            for (int i = 0; i < SIDES.length; i++) {
                if ((mask & (1 << i)) != 0) {
                    shape = Shapes.or(shape, spigots[i]);
                }
            }
            shapes[mask] = shape;
        }
        return shapes;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction.getAxis().isVertical() || !(level instanceof Level world)) {
            return state;
        }
        return state.setValue(SIDE_PROPERTIES.get(direction), connectsTo(world, pos, direction));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = defaultBlockState();
        for (Direction side : SIDES) {
            state = state.setValue(SIDE_PROPERTIES.get(side), connectsTo(level, pos, side));
        }
        return state;
    }

    private static boolean connectsTo(Level level, BlockPos pos, Direction side) {
        BlockPos neighborPos = pos.relative(side);
        if (!level.hasChunkAt(neighborPos)) {
            return false;
        }
        IEssentiaTransport node = EssentiaAccess.transport(level, neighborPos, side.getOpposite());
        return node != null && node.isConnectable(side.getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAlembic(pos, state);
    }

    @Override
    public boolean applyLabel(Player player, BlockPos pos, Direction face, ItemStack stack) {
        if (!(player.level().getBlockEntity(pos) instanceof BlockEntityAlembic alembic)) {
            return false;
        }
        if (face.getAxis().isVertical()) {
            return false;
        }
        ResourceKey<IAspect> chosen = LabelledVesselActions.aspectToLabel(stack, alembic.aspectFilterKey(), alembic.aspectKey(), alembic.amount());
        if (chosen == null) {
            return false;
        }
        alembic.setAspectFilter(chosen);
        alembic.aimSpout(face);
        LabelledVesselActions.playLabelSound(player.level(), pos);
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAlembic alembic)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (alembic.aspectFilterKey() != null && hit.getDirection() == alembic.spoutSide()) {
            alembic.setAspectFilter(null);
            LabelledVesselActions.removeLabel(level, pos, hit.getDirection());
        } else {
            LabelledVesselActions.pourOut(level, pos, alembic.amount());
            alembic.clearAspect();
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int mask = 0;
        for (int i = 0; i < SIDES.length; i++) {
            if (state.getValue(SIDE_PROPERTIES.get(SIDES[i]))) {
                mask |= 1 << i;
            }
        }
        return SHAPES[mask];
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAlembic alembic)) {
            return 0;
        }
        int stored = alembic.amount();
        return (int) Math.floor((double) stored / BlockEntityAlembic.CAPACITY * COMPARATOR_STEPS) + (stored > 0 ? 1 : 0);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BlockEntityAlembic alembic && alembic.aspectFilterKey() != null) {
            popResource(level, pos, new ItemStack(TTItems.LABEL.get()));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
