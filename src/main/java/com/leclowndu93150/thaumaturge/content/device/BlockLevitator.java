package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockLevitator extends BaseEntityBlock {
    public static final MapCodec<BlockLevitator> CODEC = simpleCodec(BlockLevitator::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private static final int BACK_INSET_PX = 2;
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes();

    public BlockLevitator(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(BlockStateProperties.ENABLED, true));
    }

    private static Map<Direction, VoxelShape> buildShapes() {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.values()) {
            double minX = facing.getStepX() > 0 ? BACK_INSET_PX : 0;
            double maxX = 16 - (facing.getStepX() < 0 ? BACK_INSET_PX : 0);
            double minY = facing.getStepY() > 0 ? BACK_INSET_PX : 0;
            double maxY = 16 - (facing.getStepY() < 0 ? BACK_INSET_PX : 0);
            double minZ = facing.getStepZ() > 0 ? BACK_INSET_PX : 0;
            double maxZ = 16 - (facing.getStepZ() < 0 ? BACK_INSET_PX : 0);
            shapes.put(facing, Block.box(minX, minY, minZ, maxX, maxY, maxZ));
        }
        return shapes;
    }

    @Override
    protected MapCodec<BlockLevitator> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BlockStateProperties.ENABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getNearestLookingDirection().getOpposite();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            facing = facing.getOpposite();
        }
        return defaultBlockState().setValue(FACING, facing).setValue(BlockStateProperties.ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        boolean enabled = !level.hasNeighborSignal(pos);
        if (enabled != state.getValue(BlockStateProperties.ENABLED)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.ENABLED, enabled), 3);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityLevitator levitator)) {
            return InteractionResult.PASS;
        }
        levitator.increaseRange(player);
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.KEY.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityLevitator(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.LEVITATOR.get(), BlockEntityLevitator::tick);
    }
}
