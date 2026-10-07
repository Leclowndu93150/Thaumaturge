package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.leclowndu93150.thaumaturge.content.research.DeviceGate;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockThaumatorium extends BaseEntityBlock {
    public static final MapCodec<BlockThaumatorium> CODEC = simpleCodec(BlockThaumatorium::new);

    private static final Map<Direction, VoxelShape> SHAPES = DeviceShapes.facingShapesFromNorth(Shapes.or(box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), box(2.0, 2.0, 2.0, 14.0, 13.0, 14.0),
            box(1.0, 2.0, 1.0, 4.0, 13.0, 4.0), box(12.0, 2.0, 1.0, 15.0, 13.0, 4.0), box(1.0, 2.0, 12.0, 4.0, 13.0, 15.0), box(12.0, 2.0, 12.0, 15.0, 13.0, 15.0),
            box(0.0, 5.0, 5.0, 16.0, 11.0, 11.0), box(0.0, 21.0, 5.0, 16.0, 27.0, 11.0), box(5.0, 5.0, 14.0, 11.0, 11.0, 16.0), box(5.0, 21.0, 13.0, 11.0, 27.0, 16.0),
            box(5.0, 2.0, 0.0, 11.0, 6.0, 2.0), box(5.0, 7.0, 1.0, 11.0, 12.0, 2.0), box(2.0, 13.0, 2.0, 14.0, 15.0, 14.0), box(3.0, 15.0, 3.0, 13.0, 26.0, 13.0), box(2.0, 15.0, 2.0, 4.0, 26.0, 4.0),
            box(12.0, 15.0, 2.0, 14.0, 26.0, 4.0), box(2.0, 15.0, 12.0, 4.0, 26.0, 14.0), box(12.0, 15.0, 12.0, 14.0, 26.0, 14.0), box(2.0, 26.0, 2.0, 14.0, 28.0, 14.0),
            box(6.0, 28.0, 6.0, 10.0, 30.0, 10.0), box(5.0, 30.0, 5.0, 11.0, 31.0, 11.0)));

    public BlockThaumatorium(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<BlockThaumatorium> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        return pos.getY() < level.getMaxY() && level.getBlockState(pos.above()).canBeReplaced(context)
                ? defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite())
                : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), TTBlocks.THAUMATORIUM_TOP.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    public static VoxelShape shapeFacing(Direction facing) {
        return SHAPES.get(facing);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFacing(state.getValue(HorizontalDirectionalBlock.FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityThaumatorium(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && !DeviceGate.passes(player, TTIds.rl("thaumatorium"))) {
            return InteractionResult.SUCCESS_SERVER;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof BlockEntityThaumatorium thaumatorium) {
            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("block.thaumaturge.thaumatorium");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
                    return new MenuThaumatorium(containerId, inventory, thaumatorium);
                }
            }, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        if (level.getBlockState(pos.above()).is(TTBlocks.THAUMATORIUM_TOP.get())) {
            level.destroyBlock(pos.above(), true);
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TTBlockEntities.THAUMATORIUM.get(), BlockEntityThaumatorium::serverTick);
    }
}
