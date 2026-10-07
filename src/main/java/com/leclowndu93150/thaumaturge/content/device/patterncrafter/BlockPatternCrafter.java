package com.leclowndu93150.thaumaturge.content.device.patterncrafter;

import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockPatternCrafter extends BaseEntityBlock {
    public static final MapCodec<BlockPatternCrafter> CODEC = simpleCodec(BlockPatternCrafter::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final Map<Direction, VoxelShape> SHAPES = DeviceShapes.facingShapesFromNorth(Shapes.or(
            box(0.0, 8.0, 0.0, 16.0, 16.0, 2.0),
            box(0.0, 10.0, 2.0, 2.0, 16.0, 16.0),
            box(2.0, 0.0, 2.0, 14.0, 1.0, 14.0),
            box(2.0, 10.0, 2.0, 16.0, 11.0, 16.0),
            box(2.0, 11.0, 14.0, 16.0, 16.0, 16.0),
            box(4.0, 1.0, 4.0, 12.0, 10.0, 12.0),
            box(6.0, 3.0, 12.0, 10.0, 7.0, 14.0),
            box(6.0, 6.0, 3.0, 10.0, 10.0, 4.0),
            box(14.0, 11.0, 2.0, 16.0, 16.0, 14.0)));

    public BlockPatternCrafter(Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BlockStateProperties.ENABLED, true));
    }

    @Override
    protected MapCodec<BlockPatternCrafter> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BlockStateProperties.ENABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        if (context.getPlayer() != null) {
            facing = context.getPlayer().getDirection();
        }
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(BlockStateProperties.ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(
            BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean movedByPiston) {
        boolean enabled = !level.hasNeighborSignal(pos);
        if (enabled != state.getValue(BlockStateProperties.ENABLED)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.ENABLED, enabled), 3);
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityPatternCrafter(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return type == TTBlockEntities.PATTERN_CRAFTER.get()
                ? (tickLevel, pos, tickState, crafter) ->
                        ((BlockEntityPatternCrafter) crafter).tick(tickLevel, pos, tickState)
                : null;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityPatternCrafter crafter)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        crafter.cycle();
        level.playSound(
                null,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                TTSounds.KEY.get(),
                SoundSource.BLOCKS,
                0.5F,
                1.0F);
        return InteractionResult.SUCCESS;
    }
}
