package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.content.device.BlockInlay;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockPedestal extends BaseEntityBlock {
    public static final MapCodec<BlockPedestal> CODEC = simpleCodec(BlockPedestal::new);
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 15);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0),
            Block.box(2.0, 12.0, 2.0, 14.0, 16.0, 14.0));
    private static final VoxelShape ANCIENT_AND_ELDRITCH_SHAPE = Shapes.or(
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
            BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean movedByPiston) {
        if (!level.isClientSide()) {
            BlockInlay.updateNetwork(level, pos);
        }
    }

    @Override
    protected MapCodec<BlockPedestal> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityPedestal(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.is(TCBlocks.PEDESTAL_ANCIENT.get()) || state.is(TCBlocks.PEDESTAL_ELDRITCH.get())
                ? ANCIENT_AND_ELDRITCH_SHAPE
                : SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return swap(level, pos, player, InteractionHand.MAIN_HAND);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        InteractionResult result = swap(level, pos, player, hand);
        if (result == InteractionResult.PASS) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return result.consumesAction() ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof BlockEntityPedestal pedestal
                    && !pedestal.getItem().isEmpty()) {
                Containers.dropItemStack(
                        level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, pedestal.getItem());
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
