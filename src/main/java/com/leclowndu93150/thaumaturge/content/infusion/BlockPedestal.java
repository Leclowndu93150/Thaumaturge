package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.content.device.BlockInlay;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class BlockPedestal extends BaseEntityBlock {
    public static final MapCodec<BlockPedestal> CODEC = simpleCodec(BlockPedestal::new);
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 15);

    private static final VoxelShape SHAPE_TALL = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0),
            Block.box(2.0, 12.0, 2.0, 14.0, 16.0, 14.0));
    private static final VoxelShape SHAPE_SHORT = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(2.0, 4.0, 2.0, 14.0, 8.0, 14.0),
            Block.box(4.0, 8.0, 4.0, 12.0, 12.0, 12.0));

    public BlockPedestal(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CHARGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override
    protected void neighborChanged(
            @NonNull BlockState state,
            Level level,
            @NonNull BlockPos pos,
            @NonNull Block neighborBlock,
            @Nullable Orientation orientation,
            boolean movedByPiston) {
        if (!level.isClientSide()) {
            BlockInlay.updateNetwork(level, pos);
        }
    }

    @Override
    protected @NonNull MapCodec<BlockPedestal> codec() {
        return CODEC;
    }

    @Override
    public @NonNull BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new BlockEntityPedestal(pos, state);
    }

    @Override
    protected @NonNull VoxelShape getShape(
            BlockState state,
            @NonNull BlockGetter level,
            @NonNull BlockPos pos,
            @NonNull CollisionContext context) {
        // Arcane pedestal is a full block tall; ancient/eldritch pedestals are shorter
        // (their models stop at y=12), so they need a matching shorter collision box.
        return state.getBlock() == TCBlocks.PEDESTAL_ARCANE.get() ? SHAPE_TALL : SHAPE_SHORT;
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Player player,
            @NonNull BlockHitResult hit) {
        return swap(level, pos, player, InteractionHand.MAIN_HAND);
    }

    @Override
    protected @NonNull InteractionResult useItemOn(
            @NonNull ItemStack stack,
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Player player,
            @NonNull InteractionHand hand,
            @NonNull BlockHitResult hit) {
        return swap(level, pos, player, hand);
    }

    private InteractionResult swap(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityPedestal pedestal)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack held = player.getItemInHand(hand);
        ItemStack current = pedestal.getItem();
        if (current.isEmpty() && held.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!current.isEmpty()) {
            if (!player.getInventory().add(current)) {
                player.drop(current, false);
            }
            pedestal.setItem(ItemStack.EMPTY);
        }
        if (!held.isEmpty()) {
            pedestal.setItem(held.copyWithCount(1));
            held.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            @NonNull BlockState state,
            ServerLevel level,
            @NonNull BlockPos pos,
            boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof BlockEntityPedestal pedestal
                && !pedestal.getItem().isEmpty()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, pedestal.getItem());
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}
